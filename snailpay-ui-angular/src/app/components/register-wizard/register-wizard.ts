import { Component } from '@angular/core';

import { PersonalInfoStepComponent } from '../personal-info-step/personal-info-step';
import { VerificationStepComponent } from '../verification-step/verification-step';
import { WizardStep } from '../../models/wizard.models';
import { KycStepComponent } from '../kyc-step-component/kyc-step-component';
import { WizardComponent } from '../wizard/wizard';

@Component({
  selector: 'app-register-wizard',
  imports: [WizardComponent],
  providers: [RegistrationStateService],
  templateUrl: './register-wizard.html',
})
export class RegisterWizardComponent {
  protected readonly steps: readonly WizardStep[] = [
    { id: 'personal-info', component: PersonalInfoStepComponent },
    { id: 'verification', component: VerificationStepComponent },
    { id: 'kyc', component: KycStepComponent },
  ];
}