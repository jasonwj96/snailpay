import { Component, signal } from '@angular/core';
import { AuthFormComponent } from '../../components/authform/authform';

@Component({
  selector: 'app-loginpage',
  imports: [AuthFormComponent],
  templateUrl: './loginpage.html',
  styleUrl: './loginpage.css',
})
export class LoginPage { }
