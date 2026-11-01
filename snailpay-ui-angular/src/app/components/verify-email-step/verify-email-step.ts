import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, catchError, switchMap, takeWhile, timer } from 'rxjs';
import { CustomerRegistrationService } from '../../services/customer-registration';
import { WizardComponent } from '../wizard/wizard';
import { RegistrationStateService } from '../../services/registration-state-service';

const RESEND_COOLDOWN_SECONDS = 60;
const POLL_INTERVAL_MS = 4000;

@Component({
  selector: 'app-verify-email-step',
  templateUrl: './verify-email-step.html',
})
export class VerifyEmailStepComponent implements OnInit {
  private readonly wizard = inject(WizardComponent);
  private readonly registration = inject(RegistrationStateService);
  private readonly registrationService = inject(CustomerRegistrationService);
  private readonly destroyRef = inject(DestroyRef);

  private readonly externalId = computed(() => this.registration.customer()?.externalId);
  protected readonly email = computed(() => this.registration.customer()?.email ?? '');

  protected readonly verified = signal(false);
  protected readonly sending = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly infoMessage = signal<string | null>(null);

  // Stored as an end timestamp, not a decrementing counter, so it stays
  // accurate when the browser throttles timers in a background tab. This
  // matters here more than in the phone step: the user leaves this tab to
  // open their inbox.
  private readonly now = signal(Date.now());
  private readonly resendAvailableAt = signal(0);
  private timerId: ReturnType<typeof setInterval> | null = null;

  protected readonly secondsLeft = computed(() =>
    Math.max(0, Math.ceil((this.resendAvailableAt() - this.now()) / 1000))
  );
  protected readonly canResend = computed(() => this.secondsLeft() === 0 && !this.sending());
  protected readonly countdownLabel = computed(() => {
    const seconds = this.secondsLeft();
    return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, '0')}`;
  });

  constructor() {
    this.destroyRef.onDestroy(() => this.stopTimer());
  }

  ngOnInit(): void {
    this.sendEmail();
    this.pollStatus();
  }

  protected resend(): void {
    if (!this.canResend()) {
      return;
    }
    this.sendEmail(true);
  }

  protected continueToNextStep(): void {
    if (this.verified()) {
      this.wizard.goToNextStep();
    }
  }

  private sendEmail(isResend = false): void {
    const externalId = this.externalId();
    if (!externalId) {
      this.errorMessage.set('Something went wrong. Please restart your registration.');
      return;
    }

    this.sending.set(true);
    this.errorMessage.set(null);
    this.infoMessage.set(null);

    this.registrationService.sendEmailVerification(externalId).subscribe({
      next: () => {
        this.sending.set(false);
        this.startCooldown();
        if (isResend) {
          this.infoMessage.set('A new link has been sent. Older links no longer work.');
        }
      },
      error: (error: HttpErrorResponse) => {
        this.sending.set(false);
        if (error.status === 409) {
          // Already verified (e.g. the user resumed the wizard): nothing to send.
          this.markVerified();
        } else if (error.status === 429) {
          this.startCooldown();
          this.errorMessage.set('Too many requests. Please wait before asking for another email.');
        } else {
          this.errorMessage.set("We couldn't send the email. Please try again.");
        }
      },
    });
  }

  // The link is usually opened in another tab or on another device, so this
  // step can't observe it directly. It polls until the backend reports the
  // email as verified, then stops.
  private pollStatus(): void {
    const externalId = this.externalId();
    if (!externalId) {
      return;
    }

    timer(0, POLL_INTERVAL_MS)
      .pipe(
        switchMap(() =>
          this.registrationService.checkVerificationStatus(externalId).pipe(catchError(() => EMPTY))
        ),
        takeWhile(status => !status.emailVerified, true),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(status => {
        if (status.emailVerified) {
          this.markVerified();
        }
      });
  }

  private markVerified(): void {
    this.verified.set(true);
    this.errorMessage.set(null);
    this.infoMessage.set(null);
    this.registration.emailVerified.set(true);
    this.stopTimer();
  }

  private startCooldown(): void {
    this.stopTimer();
    this.resendAvailableAt.set(Date.now() + RESEND_COOLDOWN_SECONDS * 1000);
    this.now.set(Date.now());
    this.timerId = setInterval(() => {
      this.now.set(Date.now());
      if (this.secondsLeft() === 0) {
        this.stopTimer();
      }
    }, 1000);
  }

  private stopTimer(): void {
    if (this.timerId !== null) {
      clearInterval(this.timerId);
      this.timerId = null;
    }
  }
}