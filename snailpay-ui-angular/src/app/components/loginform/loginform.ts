import { Component, signal } from '@angular/core';
import { LoginForm } from '../../models/LoginForm';

@Component({
  selector: 'app-loginform',
  imports: [],
  templateUrl: './loginform.html',
})
export class Loginform {
  loginModel = signal<LoginForm>({
    email: '',
    password: '',
  });

  updateEmail(value: string) {
    this.loginModel.update((form) => ({ ...form, email: value }));
  }

  updatePassword(value: string) {
    this.loginModel.update((form) => ({ ...form, password: value }));
  }

  onSubmit(event: Event) {
    event.preventDefault();
    console.log('Submitting:', this.loginModel());
  }
}