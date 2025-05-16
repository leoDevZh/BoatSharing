import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, ResolveFn, Router, RouterStateSnapshot} from '@angular/router';

import {userResolver} from './user.resolver';
import {UserControllerService, UserDTO} from '../api';
import {Observable, of, throwError} from 'rxjs';
import {ToastyService} from '../services/toasty/toasty.service';

describe('userResolver', () => {
  let userService: jasmine.NonTypedSpyObj<UserControllerService>
  let route: jasmine.SpyObj<ActivatedRouteSnapshot>
  let state: jasmine.SpyObj<RouterStateSnapshot>
  let router: jasmine.SpyObj<Router>
  let toastyService: jasmine.SpyObj<ToastyService>

  const executeResolver: ResolveFn<UserDTO> = (...resolverParameters) =>
    TestBed.runInInjectionContext(() => userResolver(...resolverParameters));

  beforeEach(() => {
    userService = jasmine.createSpyObj('UserControllerService', ['getUser'])
    userService.getUser.and.returnValue(of({username: "user"} as UserDTO))
    route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], {params: {}})
    state = jasmine.createSpyObj('RouterStateSnapshot', [], {url: '/'})
    router = jasmine.createSpyObj('Router', ['navigate'])
    toastyService = jasmine.createSpyObj('ToastyService', ['addErrorNotification'])

    TestBed.configureTestingModule({
      providers: [
        {provide: UserControllerService, useValue: userService},
        {provide: Router, useValue: router},
        {provide: ToastyService, useValue: toastyService}
      ]
    });
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });

  it('should resolve user data from the service', async () => {
    const res$ = await executeResolver(route, state) as Observable<UserDTO>
    res$.subscribe({
      next: () => expect(userService.getUser).toHaveBeenCalled()
    })
  });

  it('should handle error', async () => {
    userService.getUser.and.returnValue(throwError(() => "some error"))
    const res$ = await executeResolver(route, state) as Observable<UserDTO>
    res$.subscribe({
      next: () => fail('Expected resolver to throw an error'),
      error: (err) => {
        expect(router.navigate).toHaveBeenCalledWith(['/error'])
        expect(toastyService.addErrorNotification).toHaveBeenCalled()
      }
    })
  })
});
