import { HttpClient, HttpEventType, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, filter, map } from 'rxjs';
import {
  KycStatusResponse,
  UploadUrlRequest,
  UploadUrlResponse,
  KycDocument,
  KycSubmissionRequest,
} from '../models/kys.models';

@Injectable({ providedIn: 'root' })
export class KycService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/onboarding/kyc';

  getStatus(): Observable<KycStatusResponse> {
    return this.http.get<KycStatusResponse>(`${this.baseUrl}/status`);
  }

  requestUploadUrl(request: UploadUrlRequest): Observable<UploadUrlResponse> {
    return this.http.post<UploadUrlResponse>(`${this.baseUrl}/documents/upload-url`, request);
  }

  // Uploads straight to S3 and emits progress as a percentage (0-100).
  // This request goes to amazonaws.com, not your API: any interceptor that adds
  // an Authorization header to every request must skip it, because S3 rejects a
  // request that carries both a presigned signature and a bearer token.
  uploadToS3(target: UploadUrlResponse, file: File): Observable<number> {
    return this.http
      .put(target.uploadUrl, file, {
        headers: new HttpHeaders(target.headers),
        reportProgress: true,
        observe: 'events',
      })
      .pipe(
        map((event) => {
          if (event.type === HttpEventType.UploadProgress && event.total) {
            return Math.round((100 * event.loaded) / event.total);
          }
          return event.type === HttpEventType.Response ? 100 : null;
        }),
        filter((percent): percent is number => percent !== null),
      );
  }

  completeUpload(documentId: string): Observable<KycDocument> {
    return this.http.post<KycDocument>(`${this.baseUrl}/documents/${documentId}/complete`, {});
  }

  submit(request: KycSubmissionRequest): Observable<KycStatusResponse> {
    return this.http.post<KycStatusResponse>(`${this.baseUrl}/submit`, request);
  }
}
