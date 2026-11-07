import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { WizardComponent } from '../../components/wizard/wizard';
import { KycStepComponent } from '../../components/kyc-step/kyc-step';
import { WizardStep } from '../../models/Wizard.models';
import { RegistrationStateService } from '../../services/registration-state-service';
import { PersonalInfoStepComponent } from '../../components/personal-info-step/personal-info-step';
import { PhoneVerificationStep } from '../../components/phone-verification-step/phone-verification-step';
import { VerifyEmailStepComponent } from '../../components/verify-email-step/verify-email-step';

@Component({
  selector: 'app-registerpage',
  imports: [WizardComponent],
  providers: [RegistrationStateService],
  templateUrl: './registerpage.html',
})
export class RegisterPage {
  private readonly router = inject(Router);

  protected readonly steps: readonly WizardStep[] = [
    { id: 'personal-info', component: PersonalInfoStepComponent, label: 'General Information' },
    { id: 'verify-email', component: VerifyEmailStepComponent , label: 'Email Verification'},
    { id: 'phone-verification', component: PhoneVerificationStep , label: 'Phone Verification'},
    { id: 'kyc', component: KycStepComponent , label: 'Additional Information'},
  ];

  protected onCompleted(): void {
    this.router.navigate(['/']);
  }
}