import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KycStepComponent } from './kyc-step';

describe('KycStepComponent', () => {
  let component: KycStepComponent;
  let fixture: ComponentFixture<KycStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KycStepComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(KycStepComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
