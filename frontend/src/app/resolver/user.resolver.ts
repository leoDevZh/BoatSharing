import {ResolveFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {UserControllerService, UserDTO} from '../api';
import {catchError, Observable, throwError} from 'rxjs';
import {ToastyService} from '../services/toasty/toasty.service';

export const userResolver: ResolveFn<UserDTO> = (route, state): Observable<UserDTO> => {
  const userService = inject(UserControllerService);
  const router = inject(Router);
  const toastyService = inject(ToastyService)

  return userService.getUser()
    .pipe(
      catchError(err => {
        toastyService.addErrorNotification("Unerwarteter Fehler: Userdaten konnten nicht geladen werden")
        router.navigate(['/error'])
        return throwError(() => err)
      })
    );
};
