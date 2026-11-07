// dev-customer.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject, isDevMode } from '@angular/core';
import { DevCustomerService } from '../services/dev-customer-service';

export const devCustomerInterceptor: HttpInterceptorFn = (req, next) => {
  const externalId = inject(DevCustomerService).externalId();
  // Only for our own API, never for the S3 upload URL.
  if (isDevMode() && externalId && req.url.startsWith('/api/')) {
    req = req.clone({ setHeaders: { 'X-Dev-External-Id': externalId } });
  }
  return next(req);
};