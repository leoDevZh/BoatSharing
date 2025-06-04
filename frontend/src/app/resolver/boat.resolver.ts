import {ResolveFn, Router} from '@angular/router';
import {BoatControllerService, BoatId} from '../api';
import {inject} from '@angular/core';
import {ToastyService} from '../services/toasty/toasty.service';
import {catchError, throwError} from 'rxjs';
import {ErrorInfo} from '../model/ErrorInfo';

export const boatResolver: ResolveFn<BoatId> = (route, state) => {
  const boatControllerService = inject(BoatControllerService)
  const router = inject(Router)
  const toastyNotification = inject(ToastyService)

  return boatControllerService.getBoatFromUser()
    .pipe(
      catchError((err: ErrorInfo) => {
        toastyNotification.addErrorNotification(err.msg ?? "Unerwarteter Fehler: BootId konnten nicht geladen werden")
        router.navigate(['/error'])
        return throwError(() => err)
      })
    );
};
