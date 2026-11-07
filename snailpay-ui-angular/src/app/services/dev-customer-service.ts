import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class DevCustomerService {
  readonly externalId = signal<string | null>(null);
}