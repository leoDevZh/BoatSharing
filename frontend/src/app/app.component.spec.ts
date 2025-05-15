import {TestBed} from '@angular/core/testing';
import {AppComponent} from './app.component';
import {AuthenticationControllerService, AuthenticationResponseTO} from './api';
import {AuthService} from './services/AuthService/auth.service';
import {of, throwError} from 'rxjs';
import {Router} from '@angular/router';

describe('AppComponent', () => {
  let authController: jasmine.NonTypedSpyObj<AuthenticationControllerService>
  let authService: jasmine.SpyObj<AuthService>
  let router: jasmine.SpyObj<Router>

  beforeEach(async () => {
    authController = jasmine.createSpyObj('AuthenticationControllerService', ['refresh'])
    authService = jasmine.createSpyObj('AuthService', ['setToken'])
    router = jasmine.createSpyObj('Router', ['navigate'])
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        {provide: AuthenticationControllerService, useValue: authController},
        {provide: AuthService, useValue: authService},
        {provide: Router, useValue: router}
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should call services on successful refresh', () => {
    const mockResponse: AuthenticationResponseTO = {token: 'some-Token'}
    authController.refresh.and.returnValue(of(mockResponse))

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges()

    expect(authController.refresh).toHaveBeenCalledTimes(1)
    expect(authService.setToken).toHaveBeenCalledWith(mockResponse.token)
    expect(router.navigate).toHaveBeenCalledWith(['/home'])
  })

  it('should not call services on error refresh', () => {
    authController.refresh.and.returnValue(throwError(() => 'some Error'))

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges()

    expect(authController.refresh).toHaveBeenCalledTimes(1)
    expect(authService.setToken).not.toHaveBeenCalled()
  })
});
