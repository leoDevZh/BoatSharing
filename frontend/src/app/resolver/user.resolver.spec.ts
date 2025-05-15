import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from '@angular/router';

import {userResolver} from './user.resolver';
import {UserControllerService, UserDTO} from '../api';

fdescribe('userResolver', () => {
  let userService: jasmine.NonTypedSpyObj<UserControllerService>
  let route: jasmine.SpyObj<ActivatedRouteSnapshot>
  let state: jasmine.SpyObj<RouterStateSnapshot>

  const executeResolver: ResolveFn<UserDTO> = (...resolverParameters) =>
    TestBed.runInInjectionContext(() => userResolver(...resolverParameters));

  beforeEach(() => {
    userService = jasmine.createSpyObj('UserControllerService', ['getUser'])
    route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], {params: {}})
    state = jasmine.createSpyObj('RouterStateSnapshot', [], {url: '/'})

    TestBed.configureTestingModule({
      providers: [{provide: UserControllerService, useValue: userService}]
    });
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });

  it('should resolve user data from the service', () => {
    executeResolver(route, state)
    expect(userService.getUser).toHaveBeenCalled()
  });
});
