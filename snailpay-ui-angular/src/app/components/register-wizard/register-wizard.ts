import { Component, inject } from '@angular/core';
import { RegistrationWizardService } from '../../services/registration-wizard';
import { PersonalInfoStepComponent } from '../personal-info-step/personal-info-step';
import { VerificationStepComponent } from '../verification-step/verification-step';

@Component({
  selector: 'app-register-wizard',
  imports: [PersonalInfoStepComponent, VerificationStepComponent],
  templateUrl: './register-wizard.html',
  // Scoped here (not providedIn: 'root') so wizard state is fresh every
  // time this route is entered, and discarded when the user navigates away.
  providers: [RegistrationWizardService],
})
export class RegisterWizardComponent {
  protected readonly wizard = inject(RegistrationWizardService);
  protected readonly stepIndices = Array.from({ length: this.wizard.totalSteps }, (_, i) => i);
}