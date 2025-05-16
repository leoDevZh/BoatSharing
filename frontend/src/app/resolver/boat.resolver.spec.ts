import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, ResolveFn, Router, RouterStateSnapshot} from '@angular/router';

import {boatResolver} from './boat.resolver';
import {BoatControllerService, BoatId} from '../api';
import {ToastyService} from '../services/toasty/toasty.service';
import {Observable, of, throwError} from 'rxjs';

describe('boatResolver', () => {
  let boatService: jasmine.NonTypedSpyObj<BoatControllerService>
  let route: jasmine.SpyObj<ActivatedRouteSnapshot>
  let state: jasmine.SpyObj<RouterStateSnapshot>
  let router: jasmine.SpyObj<Router>
  let toastyService: jasmine.SpyObj<ToastyService>
  const mockedBoatId = {value: 15};

  const executeResolver: ResolveFn<BoatId> = (...resolverParameters) =>
    TestBed.runInInjectionContext(() => boatResolver(...resolverParameters));

  beforeEach(() => {
    boatService = jasmine.createSpyObj('BoatControllerService', ['getBoatFromUser'])
    boatService.getBoatFromUser.and.returnValue(of(mockedBoatId))
    route = jasmine.createSpyObj('ActivatedRouteSnapshot', [], {params: {}})
    state = jasmine.createSpyObj('RouterStateSnapshot', [], {url: '/'})
    router = jasmine.createSpyObj('Router', ['navigate'])
    toastyService = jasmine.createSpyObj('ToastyService', ['addErrorNotification'])
    TestBed.configureTestingModule({
      providers: [
        {provide: BoatControllerService, useValue: boatService},
        {provide: Router, useValue: router},
        {provide: ToastyService, useValue: toastyService}
      ]
    });
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });

  it('should resolve boatId data from the service', async () => {
    const res$ = executeResolver(route, state) as Observable<BoatId>
    res$.subscribe({
      next: value => {
        expect(value).toEqual(mockedBoatId)
        expect(boatService.getBoatFromUser).toHaveBeenCalled()
      }
    })
  })

  it('should handle error', async () => {
    boatService.getBoatFromUser.and.returnValue(throwError(() => 'some error'))
    const res$ = executeResolver(route, state) as Observable<BoatId>
    res$.subscribe({
      next: () => fail('Expected resolver to throw an error'),
      error: err => {
        expect(toastyService.addErrorNotification).toHaveBeenCalled()
        expect(router.navigate).toHaveBeenCalledWith(['/error'])
      }
    })
  })
});
