import {TestBed} from '@angular/core/testing';

import {AuthService, TOKEN_KEY} from './auth.service';
import {LocalStorageService} from '../localstorage/local-storage.service';

describe('AuthService', () => {
  let service: AuthService;
  let localStorageService: jasmine.SpyObj<LocalStorageService>

  beforeEach(() => {
    localStorageService = jasmine.createSpyObj('LocalStorageService', ['get', 'set'])
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        {provide: TOKEN_KEY, useValue: 'testKey'},
        {provide: LocalStorageService, useValue: localStorageService}
      ]
    });
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get token', () => {
    const expectedToken = 'testToken'
    localStorageService.get.and.returnValue(expectedToken)

    const token = service.getToken()

    expect(token).toBe(expectedToken)
    expect(localStorageService.get).toHaveBeenCalledWith('testKey')
  })

  it('should set token', () => {
    const tokenToSet = 'testToken'

    service.setToken(tokenToSet)

    expect(localStorageService.set).toHaveBeenCalledWith('testKey', tokenToSet)
  })
});
