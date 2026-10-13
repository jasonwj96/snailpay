import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonalInfoStepComponent } from './personal-info-step';

describe('PersonalInfoStepComponent', () => {
  let component: PersonalInfoStepComponent;
  let fixture: ComponentFixture<PersonalInfoStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonalInfoStepComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PersonalInfoStepComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
