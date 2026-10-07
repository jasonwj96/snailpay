import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterWizard } from './register-wizard';

describe('RegisterWizard', () => {
  let component: RegisterWizard;
  let fixture: ComponentFixture<RegisterWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterWizard],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
