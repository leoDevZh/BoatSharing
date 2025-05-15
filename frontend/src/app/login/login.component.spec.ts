import {ComponentFixture, TestBed} from '@angular/core/testing';

import {LoginComponent} from './login.component';
import {AuthenticationControllerService} from '../api';
import {AuthService} from '../services/AuthService/auth.service';
import {Router} from '@angular/router';
import {ToastyService} from '../services/toasty/toasty.service';
import {By} from '@angular/platform-browser';
import {of, throwError} from 'rxjs';
import {ErrorInfo} from '../model/ErrorInfo';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authenticationController: jasmine.NonTypedSpyObj<AuthenticationControllerService>
  let authService: AuthService
  let router: Router
  let toastyService: ToastyService

  beforeEach(async () => {
    authenticationController = jasmine.createSpyObj('AuthenticationControllerService', ['authenticate'])
    authService = jasmine.createSpyObj('AuthService', ['setToken'])
    router = jasmine.createSpyObj('Router', ['navigate'])
    toastyService = jasmine.createSpyObj('ToastyService', ['addErrorNotification'])
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        {provide: AuthenticationControllerService, useValue: authenticationController},
        {provide: AuthService, useValue: authService},
        {provide: Router, useValue: router},
        {provide: ToastyService, useValue: toastyService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should enable button on valid form', () => {
    const loginBtn = fixture.nativeElement.querySelector('button')

    expect(component.userForm.invalid).toBeTrue();
    expect(loginBtn.disabled).toBeTrue()

    component.userForm.controls.username.setValue('testuser')
    component.userForm.controls.password.setValue('password123')

    fixture.detectChanges()

    expect(component.userForm.invalid).toBeFalse();
    expect(loginBtn.disabled).toBeFalse()
  })

  it('should call services on successful login', () => {
    const token = 'some token'
    const username = 'testUser'
    const password = 'password123'
    authenticationController.authenticate.and.returnValue(of({token: token}))
    const loginBtn = fixture.debugElement.query(By.css('button'))
    component.userForm.controls.username.setValue(username)
    component.userForm.controls.password.setValue(password)
    fixture.detectChanges()

    loginBtn.nativeElement.click()
    fixture.detectChanges()

    expect(authenticationController.authenticate).toHaveBeenCalledWith({username: username, password: password})
    expect(authService.setToken).toHaveBeenCalledWith(token)
    expect(router.navigate).toHaveBeenCalledWith(['/home'])
  });

  it('should call services on failed login', () => {
    const error: ErrorInfo = {title: 'some title', msg: 'Error msg'}
    const username = 'testUser'
    const password = 'password123'
    authenticationController.authenticate.and.returnValue(throwError(() => error))
    const loginBtn = fixture.debugElement.query(By.css('button'))
    component.userForm.controls.username.setValue(username)
    component.userForm.controls.password.setValue(password)
    fixture.detectChanges()

    loginBtn.nativeElement.click()
    fixture.detectChanges()

    expect(authenticationController.authenticate).toHaveBeenCalledWith({username: username, password: password})
    expect(authService.setToken).not.toHaveBeenCalled()
    expect(router.navigate).not.toHaveBeenCalled()
    expect(toastyService.addErrorNotification).toHaveBeenCalledWith(error.msg)
  });
});
