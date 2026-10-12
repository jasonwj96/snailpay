import { Service, computed, signal } from '@angular/core';
import { CustomerRegistrationResponse } from '../models/Registration.models';

@Service()
export class RegistrationStateService {
  readonly customer = signal<CustomerRegistrationResponse | null>(null);
  readonly emailVerified = signal(false);
  readonly phoneVerified = signal(false);
  readonly bothVerified = computed(() => this.emailVerified() && this.phoneVerified());
}