import { Routes } from '@angular/router';
import { HomePage } from './pages/homepage/homepage';
import { LoginPage } from './pages/loginpage/loginpage';
import { RegisterPage } from './pages/registerpage/registerpage';
import { VerifyEmailPage } from './pages/verify-email-page/verify-email-page';

export const routes: Routes = [
  {
    path: '',
    component: HomePage,
  },
  {
    path: 'login',
    component: LoginPage,
  },
  {
    path: 'register',
    component: RegisterPage,
  },
  { path: 'verify-email', 
    component: VerifyEmailPage 
  },
];
