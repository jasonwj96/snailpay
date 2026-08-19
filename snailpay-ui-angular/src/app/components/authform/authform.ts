import { Component, signal } from '@angular/core';
import { LoginForm } from '../../models/FormData';
import { debounce, email, form, FormField, maxLength, min, minLength, required } from '@angular/forms/signals';

@Component({
  selector: 'app-loginform',
  imports: [FormField],
  templateUrl: './authform.html',
})
export class AuthFormComponent {
  loginModel = signal<LoginForm>({
    email: '',
    password: '',
  });

  loginForm = form(this.loginModel, schemaPath => {
    debounce(schemaPath.email, 500);
    required(schemaPath.email);
    email(schemaPath.email);
    required(schemaPath.password);
    minLength(schemaPath.password, 8);
    maxLength(schemaPath.password, 128);
  });

  onSubmit(event: Event) {
    event.preventDefault();
    console.log(`Logging in with: ${this.loginForm.email().value()}, ${this.loginForm.password().value()}`);
  }
}