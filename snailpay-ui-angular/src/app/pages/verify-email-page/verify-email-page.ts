import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CustomerRegistrationService } from '../../services/customer-registration';

type PageState = 'verifying' | 'success' | 'error';

// Landing page for the link in the verification email (/verify-email?token=...).
// It confirms with a POST instead of having the email link hit the API
// directly: mail scanners and link previewers follow GET links and would
// otherwise consume the token before the user ever clicks.
@Component({
  selector: 'app-verify-email-page',
  templateUrl: './verify-email-page.html',
})
export class VerifyEmailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly registrationService = inject(CustomerRegistrationService);

  protected readonly state = signal<PageState>('verifying');

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.state.set('error');
      return;
    }

    this.registrationService.confirmEmail(token).subscribe({
      next: () => this.state.set('success'),
      error: () => this.state.set('error'),
    });
  }
}