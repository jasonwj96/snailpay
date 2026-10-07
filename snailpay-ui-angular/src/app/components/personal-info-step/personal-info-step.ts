import { Component, inject, signal } from '@angular/core';
import {
  email,
  form,
  FormField,
  maxLength,
  pattern,
  required,
} from '@angular/forms/signals';
import { RegistrationWizardService } from '../../services/registration-wizard';
import { CustomerRegistrationService } from '../../services/customer-registration';
import { CustomerRegistrationRequest } from '../../models/Registration.models';

@Component({
  selector: 'app-personal-info-step',
  imports: [FormField],
  templateUrl: './personal-info-step.html',
})
export class PersonalInfoStepComponent {
  private readonly wizard = inject(RegistrationWizardService);
  private readonly registrationService = inject(CustomerRegistrationService);

  protected readonly submitting = signal(false);
  protected readonly submitError = signal<string | null>(null);

  protected readonly model = signal<CustomerRegistrationRequest>({
    email: '',
    phone: '',
    firstName: '',
    middleName: '',
    lastName: '',
    suffix: '',
    dateOfBirth: '',
  });

  protected readonly personalInfoForm = form(this.model, schemaPath => {
    required(schemaPath.email, { message: 'Email is required' });
    email(schemaPath.email, { message: 'Enter a valid email address' });

    // E.164-ish: + followed by 7–15 digits. Matches the format your
    // sample payload uses ("+50761234567").
    required(schemaPath.phone, { message: 'Phone number is required' });
    pattern(schemaPath.phone, /^\+[1-9]\d{6,14}$/, {
      message: 'Enter a valid phone number in international format, e.g. +50761234567',
    });

    required(schemaPath.firstName, { message: 'First name is required' });
    maxLength(schemaPath.firstName, 255);

    if (schemaPath.middleName) {
      maxLength(schemaPath.middleName, 255);
    }
    
    required(schemaPath.lastName, { message: 'Last name is required' });
    maxLength(schemaPath.lastName, 255);

    // Suffix deliberately has no required/closed-list validator — see the
    // earlier design discussion: it's free text, never hard-constrained.

    required(schemaPath.dateOfBirth, { message: 'Date of birth is required' });
    // NOTE: no age/majority check here yet. The backend doesn't enforce
    // one either (date_of_birth has no CHECK constraint). Worth deciding
    // explicitly whether a minimum-age rule belongs here, server-side, or
    // both — flagged in the best-practices notes.
  });

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (!this.personalInfoForm().valid()) {
      return;
    }

    this.submitting.set(true);
    this.submitError.set(null);

    const request = this.model();

    this.registrationService.createCustomer(request).subscribe({
      next: response => {
        this.submitting.set(false);
        this.wizard.completePersonalInfoStep(request, response);
      },
      error: err => {
        this.submitting.set(false);
        // A 409 here means a duplicate email/phone (see GlobalExceptionHandler
        // on the backend) — surface something more specific than a generic
        // message once that error shape is wired up on this side too.
        this.submitError.set('Something went wrong. Please check your details and try again.');
      },
    });
  }
}