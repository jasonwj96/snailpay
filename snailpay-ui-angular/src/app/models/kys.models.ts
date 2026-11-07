export type ResidencyStatus = 'US_CITIZEN' | 'US_PERMANENT_RESIDENT' | 'NON_US_PERSON';
export type TinType = 'SSN' | 'ITIN' | 'NONE';
export type IdDocumentType =
  'DRIVERS_LICENSE' | 'STATE_ID' | 'PASSPORT' | 'PERMANENT_RESIDENT_CARD';
export type KycDocumentType = 'GOVERNMENT_ID_FRONT' | 'GOVERNMENT_ID_BACK' | 'PROOF_OF_ADDRESS';
export type KycDocumentStatus = 'PENDING_UPLOAD' | 'UPLOADED' | 'REJECTED' | 'SUPERSEDED';
export type KycStatus = 'NOT_STARTED' | 'PENDING' | 'VERIFIED' | 'FAILED' | 'REQUIRES_REVIEW';

export interface KycAddress {
  line1: string;
  line2?: string;
  city: string;
  stateProvince: string;
  postalCode: string;
  countryCode: string;
}

// Sent to POST /onboarding/kyc/submit. Contains the SSN: never log it, never
// keep it in a shared service or in browser storage.
export interface KycSubmissionRequest {
  residencyStatus: ResidencyStatus;
  citizenshipCountry: string;
  address: KycAddress;
  tinType: TinType;
  tin?: string; // digits only
  idDocumentType: IdDocumentType;
  idNumber: string;
  idIssuingCountry: string;
  idIssuingState?: string;
  idExpirationDate: string; // yyyy-MM-dd
  cipNoticeAcknowledged: boolean;
  esignConsent: boolean;
  certificationAccepted: boolean;
}

export interface UploadUrlRequest {
  documentType: KycDocumentType;
  contentType: string;
  sizeBytes: number;
}

export interface UploadUrlResponse {
  documentId: string;
  uploadUrl: string;
  headers: Record<string, string>;
  expiresAt: string;
}

export interface KycDocument {
  documentId: string;
  documentType: KycDocumentType;
  status: KycDocumentStatus;
}

export interface KycStatusResponse {
  kycStatus: KycStatus;
  documents: KycDocument[];
}

// Form state. Everything is a string/boolean so it binds to inputs directly.
export interface KycFormModel {
  residencyStatus: ResidencyStatus | '';
  citizenshipCountry: string;
  tinType: TinType;
  tin: string;
  line1: string;
  line2: string;
  city: string;
  stateProvince: string;
  postalCode: string;
  countryCode: string;
  idDocumentType: IdDocumentType | '';
  idNumber: string;
  idIssuingCountry: string;
  idIssuingState: string;
  idExpirationDate: string;
  cipNoticeAcknowledged: boolean;
  esignConsent: boolean;
  certificationAccepted: boolean;
}

export class KycFormModel {
  residencyStatus: ResidencyStatus | '' = '';
  citizenshipCountry = '';
  tinType: TinType = 'SSN';
  tin = '';
  line1 = '';
  line2 = '';
  city = '';
  stateProvince = '';
  postalCode = '';
  countryCode = 'US';
  idDocumentType: IdDocumentType | '' = '';
  idNumber = '';
  idIssuingCountry = 'US';
  idIssuingState = '';
  idExpirationDate = '';
  cipNoticeAcknowledged = false;
  esignConsent = false;
  certificationAccepted = false;
}

export const US_STATES: readonly { code: string; name: string }[] = [
  { code: 'AL', name: 'Alabama' },
  { code: 'AK', name: 'Alaska' },
  { code: 'AZ', name: 'Arizona' },
  { code: 'AR', name: 'Arkansas' },
  { code: 'CA', name: 'California' },
  { code: 'CO', name: 'Colorado' },
  { code: 'CT', name: 'Connecticut' },
  { code: 'DE', name: 'Delaware' },
  { code: 'DC', name: 'District of Columbia' },
  { code: 'FL', name: 'Florida' },
  { code: 'GA', name: 'Georgia' },
  { code: 'HI', name: 'Hawaii' },
  { code: 'ID', name: 'Idaho' },
  { code: 'IL', name: 'Illinois' },
  { code: 'IN', name: 'Indiana' },
  { code: 'IA', name: 'Iowa' },
  { code: 'KS', name: 'Kansas' },
  { code: 'KY', name: 'Kentucky' },
  { code: 'LA', name: 'Louisiana' },
  { code: 'ME', name: 'Maine' },
  { code: 'MD', name: 'Maryland' },
  { code: 'MA', name: 'Massachusetts' },
  { code: 'MI', name: 'Michigan' },
  { code: 'MN', name: 'Minnesota' },
  { code: 'MS', name: 'Mississippi' },
  { code: 'MO', name: 'Missouri' },
  { code: 'MT', name: 'Montana' },
  { code: 'NE', name: 'Nebraska' },
  { code: 'NV', name: 'Nevada' },
  { code: 'NH', name: 'New Hampshire' },
  { code: 'NJ', name: 'New Jersey' },
  { code: 'NM', name: 'New Mexico' },
  { code: 'NY', name: 'New York' },
  { code: 'NC', name: 'North Carolina' },
  { code: 'ND', name: 'North Dakota' },
  { code: 'OH', name: 'Ohio' },
  { code: 'OK', name: 'Oklahoma' },
  { code: 'OR', name: 'Oregon' },
  { code: 'PA', name: 'Pennsylvania' },
  { code: 'RI', name: 'Rhode Island' },
  { code: 'SC', name: 'South Carolina' },
  { code: 'SD', name: 'South Dakota' },
  { code: 'TN', name: 'Tennessee' },
  { code: 'TX', name: 'Texas' },
  { code: 'UT', name: 'Utah' },
  { code: 'VT', name: 'Vermont' },
  { code: 'VA', name: 'Virginia' },
  { code: 'WA', name: 'Washington' },
  { code: 'WV', name: 'West Virginia' },
  { code: 'WI', name: 'Wisconsin' },
  { code: 'WY', name: 'Wyoming' },
  { code: 'AS', name: 'American Samoa' },
  { code: 'GU', name: 'Guam' },
  { code: 'MP', name: 'Northern Mariana Islands' },
  { code: 'PR', name: 'Puerto Rico' },
  { code: 'VI', name: 'U.S. Virgin Islands' },
];

// Same rules as the backend. The server is the authority; these only give
// instant feedback.
export const SSN_PATTERN = /^(?!000|666|9\d\d)\d{3}(?!00)\d{2}(?!0000)\d{4}$/;
export const ITIN_PATTERN = /^9\d{2}(5\d|6[0-5]|7\d|8[0-8]|9[0-2]|9[4-9])\d{4}$/;
export const ZIP_PATTERN = /^\d{5}(-\d{4})?$/;
export const PO_BOX_PATTERN = /\b(p\.?\s*o\.?\s*box|post\s+office\s+box)\b/i;

export const ALLOWED_UPLOAD_TYPES = ['image/jpeg', 'image/png', 'application/pdf'] as const;
export const MAX_UPLOAD_BYTES = 10 * 1024 * 1024;
