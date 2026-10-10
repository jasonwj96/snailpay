import { TestBed } from '@angular/core/testing';

import { RegistrationWizardService } from './registration-wizard';

describe('RegistrationWizard', () => {
  let service: RegistrationWizardService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RegistrationWizardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
