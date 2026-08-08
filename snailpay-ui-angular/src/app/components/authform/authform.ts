import { Component, signal } from '@angular/core';
import { LoginForm, RegisterForm } from '../../models/FormData';
import { FieldTree, form } from '@angular/forms/signals';

@Component({
  selector: 'app-loginform',
  imports: [],
  templateUrl: './authform.html',
})
export class AuthFormComponent {
  loginModel = signal<LoginForm>({
    email: '',
    password: '',
  });

  registerModel = signal<RegisterForm>({
    email: '',
    fullname: '',
    password: '',
  });

  loginForm = form(this.loginModel);
  registerForm = form(this.registerModel);

  updateEmail(value: string) {
    this.loginModel.update(form => ({ ...form, email: value }));
  }

  updatePassword(value: string) {
    this.loginModel.update(form => ({ ...form, password: value }));
  }

  onSubmit(event: Event) {
    event.preventDefault();
    console.log('Submitting:', this.loginModel());
  }
}