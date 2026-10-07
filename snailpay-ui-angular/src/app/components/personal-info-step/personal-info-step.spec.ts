import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonalInfoStep } from './personal-info-step';

describe('PersonalInfoStep', () => {
  let component: PersonalInfoStep;
  let fixture: ComponentFixture<PersonalInfoStep>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonalInfoStep],
    }).compileComponents();

    fixture = TestBed.createComponent(PersonalInfoStep);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
