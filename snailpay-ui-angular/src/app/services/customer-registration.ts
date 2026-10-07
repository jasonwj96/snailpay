import { Service } from '@angular/core';

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CustomerRegistrationRequest,
  CustomerRegistrationResponse,
} from '../models/Registration.models';

@Service()
export class CustomerRegistrationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/customers';

  createCustomer(request: CustomerRegistrationRequest): Observable<CustomerRegistrationResponse> {
    return this.http.post<CustomerRegistrationResponse>(this.baseUrl, request);
  }

  sendEmailVerification(externalId: string): Observable<void> {
    return this.http.post<void>(`/api/email-verification/${externalId}`, {});
  }

  checkVerificationStatus(
    externalId: string
  ): Observable<{ emailVerified: boolean; phoneVerified: boolean }> {
    return this.http.get<{ emailVerified: boolean; phoneVerified: boolean }>(
      `${this.baseUrl}/${externalId}/verification-status`
    );
  }

  sendPhoneVerificationCode(externalId: string): Observable<void> {
    return this.http.post<void>(`/api/phone-verification/${externalId}/send`, {});
  }

  verifyPhoneCode(externalId: string, code: string): Observable<void> {
    return this.http.post<void>(`/api/phone-verification/${externalId}/verify`, { code });
  }
}