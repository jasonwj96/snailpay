import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VerifyEmailStep } from './verify-email-step';

describe('VerifyEmailStep', () => {
  let component: VerifyEmailStep;
  let fixture: ComponentFixture<VerifyEmailStep>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerifyEmailStep],
    }).compileComponents();

    fixture = TestBed.createComponent(VerifyEmailStep);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
