import {Component, inject} from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {AuthenticationControllerService, AuthenticationRequestTO} from '../api';
import {AuthService} from '../services/AuthService/auth.service';
import {MatInput} from '@angular/material/input';
import {MatFormField} from '@angular/material/form-field';
import {catchError, Observable, throwError} from 'rxjs';
import {Router} from '@angular/router';
import {ToastyService} from '../services/toasty/toasty.service';
import {ErrorInfo} from '../model/ErrorInfo';
import {ButtonDirective} from '../shared/button/button.directive';

interface LoginForm {
  username: FormControl<string>,
  password: FormControl<string>
}

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    FormsModule,
    MatFormField,
    MatInput,
    ButtonDirective
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  private authenticationController = inject(AuthenticationControllerService)
  private authService = inject(AuthService)
  private router = inject(Router)
  private toastyService = inject(ToastyService)

  userForm: FormGroup<LoginForm>

  constructor(fb: NonNullableFormBuilder) {
    this.userForm = fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    })
  }

  loginUser(): void {
    if (this.userForm.invalid) return;

    const formValue = this.userForm.getRawValue()
    const authenticationRequest: AuthenticationRequestTO = {
      username: formValue.username,
      password: formValue.password
    }
    this.authenticationController.authenticate(authenticationRequest)
      .pipe(
        catchError(this.handleError)
      )
      .subscribe(response => {
        this.authService.setToken(response.token)
        this.router.navigate(['/home'])
      })
  }

  private handleError = (error: ErrorInfo): Observable<never> => {
    this.toastyService.addErrorNotification(error.msg)
    return throwError(() => error)
  };
}
