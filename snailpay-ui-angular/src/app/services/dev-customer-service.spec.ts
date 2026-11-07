import { TestBed } from '@angular/core/testing';
import { DevCustomerService } from './dev-customer-service';

describe('DevCustomerService', () => {
  let service: DevCustomerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DevCustomerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
