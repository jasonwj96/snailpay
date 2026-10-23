import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PhoneVerificationStep } from './phone-verification-step';

describe('PhoneVerificationStep', () => {
  let component: PhoneVerificationStep;
  let fixture: ComponentFixture<PhoneVerificationStep>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PhoneVerificationStep],
    }).compileComponents();

    fixture = TestBed.createComponent(PhoneVerificationStep);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
