import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar';
import { HomePage } from './pages/homepage/homepage';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent, HomePage],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('snailpay-ui-ng');
}
