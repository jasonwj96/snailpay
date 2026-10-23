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
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, interval, take } from 'rxjs';
import { CustomerRegistrationService } from '../../services/customer-registration';
import { WizardComponent } from '../wizard/wizard';
import { RegistrationStateService } from '../../services/registration-state-service';

const CODE_LENGTH = 6;
const RESEND_COOLDOWN_SECONDS = 60;

const emptyDigits = (): string[] => Array.from({ length: CODE_LENGTH }, () => '');

@Component({
  selector: 'app-phone-verification-step',
  templateUrl: './phone-verification-step.html',
})
export class PhoneVerificationStep implements OnInit {
  private readonly wizard = inject(WizardComponent);
  private readonly registration = inject(RegistrationStateService);
  private readonly registrationService = inject(CustomerRegistrationService);
  private readonly destroyRef = inject(DestroyRef);

  private readonly digitInputs = viewChildren<ElementRef<HTMLInputElement>>('digitInput');
  private readonly externalId = computed(() => this.registration.customer()?.externalId);

  protected readonly slots = Array.from({ length: CODE_LENGTH }, (_, index) => index);
  protected readonly digits = signal<string[]>(emptyDigits());
  protected readonly code = computed(() => this.digits().join(''));
  protected readonly isComplete = computed(() => this.code().length === CODE_LENGTH);

  protected readonly sending = signal(false);
  protected readonly verifying = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly secondsLeft = signal(0);
  protected readonly timerLabel = computed(() => {
    const total = this.secondsLeft();
    const minutes = Math.floor(total / 60);
    const seconds = String(total % 60).padStart(2, '0');
    return `${minutes}:${seconds}`;
  });

  private cooldown?: Subscription;

  constructor() {
    afterNextRender(() => this.focusDigit(0));
  }

  ngOnInit(): void {
    this.sendCode();
  }

  protected sendCode(): void {
    const id = this.externalId();
    if (!id) {
      this.error.set('Something went wrong. Please go back and register again.');
      return;
    }
    if (this.sending() || this.secondsLeft() > 0) {
      return;
    }

    this.sending.set(true);
    this.error.set(null);

    this.registrationService.sendPhoneVerificationCode(id).subscribe({
      next: () => {
        this.sending.set(false);
        this.startCooldown();
      },
      error: err => {
        this.sending.set(false);
        this.error.set(
          err instanceof HttpErrorResponse && err.status === 429
            ? 'Too many requests. Please wait a few minutes and try again.'
            : "We couldn't send the code. Please try again."
        );
      },
    });
  }

  protected submit(event: Event): void {
    event.preventDefault();
    const id = this.externalId();
    if (!id || !this.isComplete() || this.verifying()) {
      return;
    }

    this.verifying.set(true);
    this.error.set(null);

    this.registrationService.verifyPhoneCode(id, this.code()).subscribe({
      next: () => {
        this.verifying.set(false);
        this.registration.phoneVerified.set(true);
        this.wizard.goToNextStep();
      },
      error: err => {
        this.verifying.set(false);
        this.error.set(
          err instanceof HttpErrorResponse && err.status === 429
            ? 'Too many attempts. Please wait a few minutes and try again.'
            : 'That code is incorrect or has expired. Please try again.'
        );
        this.setDigits(emptyDigits());
        this.focusDigit(0);
      },
    });
  }

  protected onInput(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const entered = input.value.replace(/\D/g, '');

    if (!entered) {
      this.setDigit(index, '');
      return;
    }
    this.fillFrom(index, entered);
  }

  protected onKeydown(event: KeyboardEvent, index: number): void {
    switch (event.key) {
      case 'Backspace':
        // Empty box: step back and clear the previous digit, like a text field would.
        if (!this.digits()[index] && index > 0) {
          event.preventDefault();
          this.setDigit(index - 1, '');
          this.focusDigit(index - 1);
        }
        break;
      case 'ArrowLeft':
        event.preventDefault();
        this.focusDigit(index - 1);
        break;
      case 'ArrowRight':
        event.preventDefault();
        this.focusDigit(index + 1);
        break;
    }
  }

  protected onPaste(event: ClipboardEvent, index: number): void {
    event.preventDefault();
    const pasted = (event.clipboardData?.getData('text') ?? '').replace(/\D/g, '');
    if (pasted) {
      this.fillFrom(index, pasted);
    }
  }

  protected selectContent(event: FocusEvent): void {
    (event.target as HTMLInputElement).select();
  }

  // Handles typing, pasting and SMS autofill (which can deliver all six
  // digits into one box). A full-length code always starts from the first box.
  private fillFrom(index: number, value: string): void {
    const start = value.length >= CODE_LENGTH ? 0 : index;
    const next = [...this.digits()];

    [...value.slice(0, CODE_LENGTH - start)].forEach((char, offset) => {
      next[start + offset] = char;
    });

    this.setDigits(next);
    this.focusDigit(Math.min(start + value.length, CODE_LENGTH - 1));
  }

  private setDigit(index: number, value: string): void {
    const next = [...this.digits()];
    next[index] = value;
    this.setDigits(next);
  }

  // The boxes are uncontrolled inputs, so every state change also writes the
  // value back to the DOM. That keeps the boxes in sync when a user types a
  // non-digit, pastes, or the code is cleared after a failed attempt.
  private setDigits(next: string[]): void {
    this.digits.set(next);
    this.digitInputs().forEach((ref, i) => {
      ref.nativeElement.value = next[i] ?? '';
    });
  }

  private focusDigit(index: number): void {
    this.digitInputs()[index]?.nativeElement.focus();
  }

  private startCooldown(): void {
    this.cooldown?.unsubscribe();
    this.secondsLeft.set(RESEND_COOLDOWN_SECONDS);

    this.cooldown = interval(1000)
      .pipe(take(RESEND_COOLDOWN_SECONDS), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.secondsLeft.update(seconds => seconds - 1));
  }
}