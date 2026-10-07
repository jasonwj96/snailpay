import { Injectable, Service, computed, signal } from '@angular/core';
import {
  CustomerRegistrationRequest,
  CustomerRegistrationResponse,
  VerificationState,
} from '../models/Registration.models';

// Adding a step: append one entry here. Nothing else in this service needs
// to change — step components read `currentStepIndex` and render themselves
// conditionally in the wizard container's template.
export const WIZARD_STEPS = ['personal-info', 'verification', 'kyc'] as const;
export type WizardStepId = (typeof WIZARD_STEPS)[number];

// Scoped to the wizard's route (provided in RegisterWizardComponent, not
// root), so state resets naturally if the user navigates away and back —
// no stale data from a previous registration attempt leaking into a new one.
@Service()
export class RegistrationWizardService {
  private readonly stepIndex = signal(0);

  readonly currentStepId = computed(() => WIZARD_STEPS[this.stepIndex()]);
  readonly currentStepIndex = this.stepIndex.asReadonly();
  readonly totalSteps = WIZARD_STEPS.length;
  readonly isFirstStep = computed(() => this.stepIndex() === 0);
  readonly isLastStep = computed(() => this.stepIndex() === WIZARD_STEPS.length - 1);

  readonly registrationRequest = signal<CustomerRegistrationRequest | null>(null);
  readonly registrationResponse = signal<CustomerRegistrationResponse | null>(null);

  readonly emailVerification = signal<VerificationState>('NOT_STARTED');
  readonly phoneVerification = signal<VerificationState>('NOT_STARTED');

  readonly bothVerified = computed(
    () => this.emailVerification() === 'VERIFIED' && this.phoneVerification() === 'VERIFIED'
  );

  goToNextStep(): void {
    if (!this.isLastStep()) {
      this.stepIndex.update(i => i + 1);
    }
  }

  goToPreviousStep(): void {
    if (!this.isFirstStep()) {
      this.stepIndex.update(i => i - 1);
    }
  }

  completePersonalInfoStep(
    request: CustomerRegistrationRequest,
    response: CustomerRegistrationResponse
  ): void {
    this.registrationRequest.set(request);
    this.registrationResponse.set(response);
    this.goToNextStep();
  }
}