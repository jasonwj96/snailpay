import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { interval, switchMap, takeWhile } from 'rxjs';
import { RegistrationWizardService } from '../../services/registration-wizard';
import { CustomerRegistrationService } from '../../services/customer-registration';

const POLL_INTERVAL_MS = 4000;

@Component({
  selector: 'app-verification-step',
  templateUrl: './verification-step.html',
})
export class VerificationStepComponent implements OnInit {
  protected readonly wizard = inject(RegistrationWizardService);
  private readonly registrationService = inject(CustomerRegistrationService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly phoneCode = signal('');
  protected readonly phoneCodeError = signal<string | null>(null);

  ngOnInit(): void {
    this.sendEmailVerification();
    this.startPollingEmailStatus();
  }

  private get externalId(): string {
    const response = this.wizard.registrationResponse();
    if (!response) {
      throw new Error('VerificationStepComponent reached without a completed registration');
    }
    return response.externalId;
  }

  protected sendEmailVerification(): void {
    this.wizard.emailVerification.set('SENDING');
    this.registrationService.sendEmailVerification(this.externalId).subscribe({
      next: () => this.wizard.emailVerification.set('SENT'),
      error: () => this.wizard.emailVerification.set('FAILED'),
    });
  }

  private startPollingEmailStatus(): void {
    interval(POLL_INTERVAL_MS)
      .pipe(
        switchMap(() => this.registrationService.checkVerificationStatus(this.externalId)),
        takeWhile(status => !status.emailVerified, true),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(status => {
        if (status.emailVerified) {
          this.wizard.emailVerification.set('VERIFIED');
        }
      });
  }

  protected sendPhoneCode(): void {
    this.wizard.phoneVerification.set('SENDING');
    this.registrationService.sendPhoneVerificationCode(this.externalId).subscribe({
      next: () => this.wizard.phoneVerification.set('SENT'),
      error: () => this.wizard.phoneVerification.set('FAILED'),
    });
  }

  protected submitPhoneCode(): void {
    const code = this.phoneCode().trim();
    if (code.length !== 6) {
      this.phoneCodeError.set('Enter the 6-digit code');
      return;
    }

    this.phoneCodeError.set(null);
    this.registrationService.verifyPhoneCode(this.externalId, code).subscribe({
      next: () => this.wizard.phoneVerification.set('VERIFIED'),
      error: () => this.phoneCodeError.set('Incorrect code. Please try again.'),
    });
  }

  protected continueToNextStep(): void {
    if (this.wizard.bothVerified()) {
      this.wizard.goToNextStep();
    }
  }
}