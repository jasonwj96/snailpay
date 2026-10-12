import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { WizardComponent } from '../../components/wizard/wizard';
import { KycStepComponent } from '../../components/kyc-step-component/kyc-step-component';
import { WizardStep } from '../../models/Wizard.models';
import { RegistrationStateService } from '../../services/registration-state-service';
import { PersonalInfoStepComponent } from '../../components/personal-info-step-component/personal-info-step-component';
import { VerificationStepComponent } from '../../components/verification-step-component/verification-step-component';

@Component({
  selector: 'app-registerpage',
  imports: [WizardComponent],
  providers: [RegistrationStateService],
  templateUrl: './registerpage.html',
})
export class RegisterPage {
  private readonly router = inject(Router);

  protected readonly steps: readonly WizardStep[] = [
    { id: 'personal-info', component: PersonalInfoStepComponent },
    { id: 'verification', component: VerificationStepComponent },
    { id: 'kyc', component: KycStepComponent },
  ];

  protected onCompleted(): void {
    this.router.navigate(['/']);
  }
}