// pages/registerpage/registerpage.ts
import { Component } from '@angular/core';
import { RegisterWizardComponent } from '../../components/register-wizard/register-wizard';

@Component({
  selector: 'app-registerpage',
  imports: [RegisterWizardComponent],
  templateUrl: './registerpage.html',
})
export class RegisterPage {}