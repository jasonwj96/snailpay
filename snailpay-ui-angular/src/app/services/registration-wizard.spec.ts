import { TestBed } from '@angular/core/testing';

import { RegistrationWizard } from './registration-wizard';

describe('RegistrationWizard', () => {
  let service: RegistrationWizard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RegistrationWizard);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
