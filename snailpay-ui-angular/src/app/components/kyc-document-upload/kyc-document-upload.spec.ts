import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KycDocumentUpload } from './kyc-document-upload';

describe('KycDocumentUpload', () => {
  let component: KycDocumentUpload;
  let fixture: ComponentFixture<KycDocumentUpload>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KycDocumentUpload],
    }).compileComponents();

    fixture = TestBed.createComponent(KycDocumentUpload);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
