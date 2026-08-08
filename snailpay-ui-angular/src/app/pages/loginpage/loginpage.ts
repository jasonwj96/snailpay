import { Component, signal } from '@angular/core';
import { Loginform } from '../../components/loginform/loginform';

@Component({
  selector: 'app-loginpage',
  imports: [Loginform],
  templateUrl: './loginpage.html',
  styleUrl: './loginpage.css',
})
export class LoginPage { }
