import { Routes } from '@angular/router';
import { HomePage } from './pages/homepage/homepage';
import { LoginPage } from './pages/loginpage/loginpage';

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
    component: LoginPage,
  },
];