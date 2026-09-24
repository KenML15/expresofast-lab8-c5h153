import { TestBed } from '@angular/core/testing';
import { Envio } from './envio';

describe('Envio', () => {
  let service: Envio;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Envio);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
