import { Component, inject, signal } from '@angular/core';
import { email, form, FormField, maxLength, pattern, required } from '@angular/forms/signals';
import { CustomerRegistrationRequest, emptyRegistrationForm } from '../../models/Registration.models';
import { CustomerRegistrationService } from '../../services/customer-registration';
import { WizardComponent } from '../wizard/wizard';
import { RegistrationStateService } from '../../services/registration-state-service';

@Component({
  selector: 'app-personal-info-step',
  imports: [FormField],
  templateUrl: './personal-info-step.html',
})
export class PersonalInfoStepComponent {
  private readonly wizard = inject(WizardComponent);
  private readonly registration = inject(RegistrationStateService);
  private readonly registrationService = inject(CustomerRegistrationService);

  protected readonly submitting = signal(false);
  protected readonly submitError = signal<string | null>(null);

  protected readonly suffixOptions = ['Jr.', 'Sr.', 'II', 'III', 'IV', 'V'];

  protected readonly model = signal(emptyRegistrationForm());

  protected readonly personalInfoForm = form(this.model, schemaPath => {
    required(schemaPath.email, { message: 'Email is required' });
    email(schemaPath.email, { message: 'Enter a valid email address' });

    required(schemaPath.phone, { message: 'Phone number is required' });
    pattern(schemaPath.phone, /^\+[1-9]\d{6,14}$/, {
      message: 'Enter a valid phone number in international format, e.g. +50761234567',
    });

    required(schemaPath.firstName, { message: 'First name is required' });
    maxLength(schemaPath.firstName, 255);

    maxLength(schemaPath.middleName, 255);

    required(schemaPath.lastName, { message: 'Last name is required' });
    maxLength(schemaPath.lastName, 255);

    required(schemaPath.dateOfBirth, { message: 'Date of birth is required' });
  });

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (!this.personalInfoForm().valid()) {
      return;
    }

    this.submitting.set(true);
    this.submitError.set(null);

    const { middleName, suffix, ...rest } = this.model();
    const request: CustomerRegistrationRequest = {
      ...rest,
      middleName: middleName || undefined,
      suffix: suffix || undefined,
    };

    this.registrationService.createCustomer(request).subscribe({
      next: response => {
        this.submitting.set(false);
        this.registration.customer.set(response);
        this.wizard.goToNextStep();
      },
      error: () => {
        this.submitting.set(false);
        this.submitError.set('Something went wrong. Please check your details and try again.');
      },
    });
  }
}