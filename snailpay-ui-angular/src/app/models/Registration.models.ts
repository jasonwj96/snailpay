export type VerificationState = 'NOT_STARTED' | 'SENDING' | 'SENT' | 'VERIFIED' | 'FAILED';

export type CustomerStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'LOCKED' | 'CLOSED';
export type KycStatus = 'NOT_STARTED' | 'PENDING' | 'VERIFIED' | 'FAILED' | 'REQUIRES_REVIEW';

export interface CustomerRegistrationRequest {
  email: string;
  phone: string;
  firstName: string;
  middleName : string | undefined;
  lastName: string;
  suffix: string | undefined;
  dateOfBirth: string;
}

export interface CustomerRegistrationResponse {
  email: string;
  externalId: string;
  status: CustomerStatus;
  kycStatus: KycStatus;
  createdAt: string;
  updatedAt: string;
}

export interface RegistrationFormModel {
  email: string;
  phone: string;
  firstName: string;
  middleName: string;
  lastName: string;
  suffix: string;
  dateOfBirth: string;
}

export const emptyRegistrationForm = (): RegistrationFormModel => ({
  email: '',
  phone: '',
  firstName: '',
  middleName: '',
  lastName: '',
  suffix: '',
  dateOfBirth: '',
});