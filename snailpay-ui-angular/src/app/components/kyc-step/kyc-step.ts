import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormField, form } from '@angular/forms/signals';
import { KycDocumentUploadComponent } from '../kyc-document-upload/kyc-document-upload';
import { WizardComponent } from '../wizard/wizard';
import {
  US_STATES,
  KycFormModel,
  KycDocumentType,
  ResidencyStatus,
  TinType,
  IdDocumentType,
  SSN_PATTERN,
  ITIN_PATTERN,
  PO_BOX_PATTERN,
  ZIP_PATTERN,
  KycSubmissionRequest,
} from '../../models/kys.models';
import { KycService } from '../../services/kyc-service';

@Component({
  selector: 'app-kyc-step',
  imports: [FormField, KycDocumentUploadComponent],
  templateUrl: './kyc-step.html',
})
export class KycStepComponent implements OnInit {
  private readonly wizard = inject(WizardComponent);
  private readonly kycService = inject(KycService);
  protected readonly usStateList = US_STATES;

  // The SSN lives only in this component's form model. It is cleared as soon as
  // the request succeeds and is never copied into shared state or storage.
  protected readonly model = signal(new KycFormModel());
  protected readonly kycForm = form(this.model);

  protected readonly submitAttempted = signal(false);
  protected readonly submitting = signal(false);
  protected readonly submitError = signal<string | null>(null);
  protected readonly submitted = signal(false);
  protected readonly uploadedDocuments = signal<ReadonlySet<KycDocumentType>>(new Set());

  protected readonly isUsPerson = computed(() => {
    const status: ResidencyStatus | '' = this.model().residencyStatus;
    return status === 'US_CITIZEN' || status === 'US_PERMANENT_RESIDENT';
  });

  // U.S. persons always identify with an SSN. Non-U.S. persons choose.
  protected readonly effectiveTinType = computed<TinType>(() =>
    this.isUsPerson() ? 'SSN' : this.model().tinType,
  );

  protected readonly needsIdBack = computed(() => {
    const type: IdDocumentType | '' = this.model().idDocumentType;
    return type !== '' && type !== 'PASSPORT';
  });

  protected readonly needsIssuingState = computed(() => {
  
    const type: IdDocumentType | '' = this.model().idDocumentType;
    return type === 'DRIVERS_LICENSE' || type === 'STATE_ID';
  });

  protected readonly isUsAddress = computed(
    () => this.model().countryCode.trim().toUpperCase() === 'US',
  );

  // Single source of truth for validation. Shown only after a submit attempt.
  protected readonly errors = computed<Record<string, string>>(() => {
    const m = this.model();
    const e: Record<string, string> = {};
    const country = /^[A-Za-z]{2}$/;

    if (!m.residencyStatus) {
      e['residencyStatus'] = 'Select your residency status.';
    }
    if (!country.test(m.citizenshipCountry.trim())) {
      e['citizenshipCountry'] = 'Enter a 2-letter country code, e.g. US.';
    }

    const tinType = this.effectiveTinType();
    const tin = m.tin.replace(/\D/g, '');
    if (tinType === 'SSN' && !SSN_PATTERN.test(tin)) {
      e['tin'] = 'Enter a valid 9-digit Social Security number.';
    }
    if (tinType === 'ITIN' && !ITIN_PATTERN.test(tin)) {
      e['tin'] = 'Enter a valid 9-digit ITIN (starts with 9).';
    }

    if (!m.line1.trim()) {
      e['line1'] = 'Street address is required.';
    } else if (PO_BOX_PATTERN.test(m.line1) || PO_BOX_PATTERN.test(m.line2)) {
      e['line1'] = 'Enter a street address. PO boxes are not accepted.';
    }
    if (!m.city.trim()) {
      e['city'] = 'City is required.';
    }
    if (!m.stateProvince.trim()) {
      e['stateProvince'] = this.isUsAddress()
        ? 'Select a state.'
        : 'State or province is required.';
    }
    if (!m.postalCode.trim()) {
      e['postalCode'] = 'Postal code is required.';
    } else if (this.isUsAddress() && !ZIP_PATTERN.test(m.postalCode.trim())) {
      e['postalCode'] = 'Enter a valid ZIP code.';
    }
    if (!country.test(m.countryCode.trim())) {
      e['countryCode'] = 'Enter a 2-letter country code.';
    }

    if (!m.idDocumentType) {
      e['idDocumentType'] = 'Select your ID type.';
    } else if (
      tinType === 'NONE' &&
      m.idDocumentType !== 'PASSPORT' &&
      m.idDocumentType !== 'PERMANENT_RESIDENT_CARD'
    ) {
      e['idDocumentType'] = 'Without a U.S. tax ID, use a passport or permanent resident card.';
    }
    if (!/^[A-Za-z0-9 -]{4,32}$/.test(m.idNumber.trim())) {
      e['idNumber'] = 'Enter the ID number (4-32 letters or digits).';
    }
    if (!country.test(m.idIssuingCountry.trim())) {
      e['idIssuingCountry'] = 'Enter a 2-letter country code.';
    }
    if (this.needsIssuingState() && !m.idIssuingState) {
      e['idIssuingState'] = 'Select the issuing state.';
    }
    if (!m.idExpirationDate || m.idExpirationDate <= new Date().toISOString().slice(0, 10)) {
      e['idExpirationDate'] = 'Enter a future expiration date. Expired IDs are not accepted.';
    }

    const uploaded = this.uploadedDocuments();
    if (!uploaded.has('GOVERNMENT_ID_FRONT')) {
      e['documents'] = 'Upload the front of your ID.';
    } else if (this.needsIdBack() && !uploaded.has('GOVERNMENT_ID_BACK')) {
      e['documents'] = 'Upload the back of your ID.';
    }

    if (!m.cipNoticeAcknowledged || !m.esignConsent || !m.certificationAccepted) {
      e['consents'] = 'Please accept all three statements to continue.';
    }
    return e;
  });

  ngOnInit(): void {
    // Resume support: show what was already uploaded, or the review screen if
    // the application was submitted earlier.
    this.kycService.getStatus().subscribe({
      next: (status) => {
        this.uploadedDocuments.set(
          new Set(
            status.documents.filter((d) => d.status === 'UPLOADED').map((d) => d.documentType),
          ),
        );
        if (status.kycStatus !== 'NOT_STARTED') {
          this.submitted.set(true);
        }
      },
      error: () => {
        // Not fatal: the user can still fill in the form.
      },
    });
  }

  protected isUploaded(type: KycDocumentType): boolean {
    return this.uploadedDocuments().has(type);
  }

  protected onDocumentUploaded(type: KycDocumentType): void {
    this.uploadedDocuments.update((current) => new Set([...current, type]));
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.submitAttempted.set(true);
    if (this.submitting() || Object.keys(this.errors()).length > 0) {
      return;
    }

    const m = this.model();
    const tinType = this.effectiveTinType();
    const request: KycSubmissionRequest = {
      residencyStatus: m.residencyStatus as ResidencyStatus,
      citizenshipCountry: m.citizenshipCountry.trim().toUpperCase(),
      address: {
        line1: m.line1.trim(),
        line2: m.line2.trim() || undefined,
        city: m.city.trim(),
        stateProvince: m.stateProvince.trim(),
        postalCode: m.postalCode.trim(),
        countryCode: m.countryCode.trim().toUpperCase(),
      },
      tinType,
      tin: tinType === 'NONE' ? undefined : m.tin.replace(/\D/g, ''),
      idDocumentType: m.idDocumentType as IdDocumentType,
      idNumber: m.idNumber.trim(),
      idIssuingCountry: m.idIssuingCountry.trim().toUpperCase(),
      idIssuingState: this.needsIssuingState() ? m.idIssuingState : undefined,
      idExpirationDate: m.idExpirationDate,
      cipNoticeAcknowledged: m.cipNoticeAcknowledged,
      esignConsent: m.esignConsent,
      certificationAccepted: m.certificationAccepted,
    };

    this.submitting.set(true);
    this.submitError.set(null);

    this.kycService.submit(request).subscribe({
      next: () => {
        this.submitting.set(false);
        this.model.set(new KycFormModel()); // wipe the SSN and ID number from memory
        this.submitted.set(true);
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        // 409/422 messages come from our own backend and are safe to show.
        const detail = error.status === 409 || error.status === 422 ? error.error?.detail : null;
        this.submitError.set(
          detail ?? 'Something went wrong. Please check your details and try again.',
        );
      },
    });
  }

  protected finish(): void {
    this.wizard.goToNextStep();
  }
}
