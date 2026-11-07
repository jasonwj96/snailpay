import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, linkedSignal, output, signal } from '@angular/core';
import { last, map, switchMap, tap } from 'rxjs';
import { KycService } from '../../services/kyc-service';
import { ALLOWED_UPLOAD_TYPES, KycDocumentType, MAX_UPLOAD_BYTES } from '../../models/kys.models';

type UploadState = 'idle' | 'uploading' | 'verifying' | 'done' | 'error';

// One reusable "pick a file, upload it to S3, confirm it" control.
@Component({
  selector: 'app-kyc-document-upload',
  templateUrl: './kyc-document-upload.html',
})
export class KycDocumentUploadComponent {
  readonly documentType = input.required<KycDocumentType>();
  readonly label = input.required<string>();
  readonly hint = input<string>();
  // True when the backend already holds an uploaded file (resume case).
  readonly alreadyUploaded = input(false);

  readonly uploaded = output<KycDocumentType>();

  private readonly kyc = inject(KycService);

  protected readonly state = linkedSignal<UploadState>(() =>
    this.alreadyUploaded() ? 'done' : 'idle'
  );
  protected readonly progress = signal(0);
  protected readonly fileName = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = ''; // lets the user pick the same file again after an error
    if (!file) {
      return;
    }

    // Instant feedback only. The server re-checks type, size and file signature.
    if (!(ALLOWED_UPLOAD_TYPES as readonly string[]).includes(file.type)) {
      this.fail('Use a JPEG, PNG or PDF file.');
      return;
    }
    if (file.size > MAX_UPLOAD_BYTES) {
      this.fail('The file is larger than 10 MB.');
      return;
    }

    this.fileName.set(file.name);
    this.errorMessage.set(null);
    this.progress.set(0);
    this.state.set('uploading');

    this.kyc
      .requestUploadUrl({
        documentType: this.documentType(),
        contentType: file.type,
        sizeBytes: file.size,
      })
      .pipe(
        switchMap(target =>
          this.kyc.uploadToS3(target, file).pipe(
            tap(percent => this.progress.set(percent)),
            last(),
            map(() => target.documentId)
          )
        ),
        tap(() => this.state.set('verifying')),
        switchMap(documentId => this.kyc.completeUpload(documentId))
      )
      .subscribe({
        next: () => {
          this.state.set('done');
          this.uploaded.emit(this.documentType());
        },
        error: (error: HttpErrorResponse) => {
          const detail = error.status === 422 || error.status === 409 ? error.error?.detail : null;
          this.fail(detail ?? 'The upload failed. Please try again.');
        },
      });
  }

  private fail(message: string): void {
    this.state.set('error');
    this.errorMessage.set(message);
  }
}
