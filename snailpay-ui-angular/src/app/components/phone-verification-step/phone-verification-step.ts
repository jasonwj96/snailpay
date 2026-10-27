import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  ElementRef,
  OnInit,
  afterNextRender,
  computed,
  inject,
  signal,
  viewChildren,
} from '@angular/core';
import { CustomerRegistrationService } from '../../services/customer-registration';
import { WizardComponent } from '../wizard/wizard';
import { RegistrationStateService } from '../../services/registration-state-service';

const CODE_LENGTH = 6;
const RESEND_COOLDOWN_SECONDS = 60;

@Component({
  selector: 'app-verify-phone-step',
  templateUrl: './phone-verification-step.html',
})
export class PhoneVerificationStep implements OnInit {
  private readonly wizard = inject(WizardComponent);
  private readonly registration = inject(RegistrationStateService);
  private readonly registrationService = inject(CustomerRegistrationService);

  private readonly digitInputs = viewChildren<ElementRef<HTMLInputElement>>('digitInput');

  protected readonly digitIndices = [...Array(CODE_LENGTH).keys()];
  protected readonly digits = signal<string[]>(Array(CODE_LENGTH).fill(''));

  private readonly code = computed(() => this.digits().join(''));
  protected readonly isCodeComplete = computed(() => this.code().length === CODE_LENGTH);

  protected readonly submitting = signal(false);
  protected readonly sending = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly infoMessage = signal<string | null>(null);

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

  private readonly externalId = computed(() => this.registration.customer()?.externalId);

  constructor() {
    inject(DestroyRef).onDestroy(() => this.stopTimer());
    afterNextRender(() => this.focusDigit(0));
  }

  ngOnInit(): void {
    this.sendCode();
  }

  protected resend(): void {
    if (!this.canResend()) {
      return;
    }
    this.clearDigits();
    this.sendCode(true);
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    const externalId = this.externalId();
    if (!externalId || !this.isCodeComplete() || this.submitting()) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.infoMessage.set(null);

    this.registrationService.verifyPhoneCode(externalId, this.code()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.registration.phoneVerified.set(true);
        this.wizard.goToNextStep();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(
          error.status === 429
            ? 'Too many attempts. Please request a new code.'
            : 'Incorrect or expired code. Please try again.'
        );
        this.clearDigits();
      },
    });
  }

  protected onInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const entered = input.value.replace(/\D/g, '');

    if (entered.length === 0) {
      this.setDigit(index, '');
      input.value = '';
      return;
    }

    // A full-length value means autofill from an SMS suggestion: spread it
    // across all boxes. Anything shorter is typed input starting at this box.
    const start = entered.length >= CODE_LENGTH ? 0 : index;
    this.fillFrom(start, entered);

    // Keep the DOM in sync for the box that fired the event, e.g. when the
    // user typed a letter that never made it into the signal.
    input.value = this.digits()[index];
  }

  protected onKeydown(index: number, event: KeyboardEvent): void {
    switch (event.key) {
      case 'Backspace':
        if (!this.digits()[index] && index > 0) {
          event.preventDefault();
          this.setDigit(index - 1, '');
          this.focusDigit(index - 1);
        }
        break;
      case 'ArrowLeft':
        if (index > 0) {
          event.preventDefault();
          this.focusDigit(index - 1);
        }
        break;
      case 'ArrowRight':
        if (index < CODE_LENGTH - 1) {
          event.preventDefault();
          this.focusDigit(index + 1);
        }
        break;
    }
  }

  protected onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const pasted = (event.clipboardData?.getData('text') ?? '').replace(/\D/g, '');
    if (pasted) {
      this.fillFrom(0, pasted);
    }
  }

  protected onFocus(event: FocusEvent): void {
    // Selecting the existing digit means typing replaces it instead of appending.
    (event.target as HTMLInputElement).select();
  }

  private sendCode(isResend = false): void {
    const externalId = this.externalId();
    if (!externalId) {
      this.errorMessage.set('Something went wrong. Please restart your registration.');
      return;
    }

    this.sending.set(true);
    this.errorMessage.set(null);
    this.infoMessage.set(null);

    this.registrationService.sendPhoneVerificationCode(externalId).subscribe({
      next: () => {
        this.sending.set(false);
        this.startCooldown();
        if (isResend) {
          this.infoMessage.set('A new code has been sent.');
        }
      },
      error: (error: HttpErrorResponse) => {
        this.sending.set(false);
        if (error.status === 429) {
          this.startCooldown();
          this.errorMessage.set('Too many requests. Please wait before asking for another code.');
        } else {
          this.errorMessage.set("We couldn't send the code. Please try again.");
        }
      },
    });
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

  private setDigit(index: number, value: string): void {
    this.digits.update(current => current.map((digit, i) => (i === index ? value : digit)));
  }

  private fillFrom(start: number, entered: string): void {
    const count = Math.min(entered.length, CODE_LENGTH - start);
    const next = [...this.digits()];
    for (let i = 0; i < count; i++) {
      next[start + i] = entered[i];
    }
    this.digits.set(next);
    this.focusDigit(Math.min(start + count, CODE_LENGTH - 1));
  }

  private clearDigits(): void {
    this.digits.set(Array(CODE_LENGTH).fill(''));
    this.focusDigit(0);
  }

  private focusDigit(index: number): void {
    this.digitInputs()[index]?.nativeElement.focus();
  }
}