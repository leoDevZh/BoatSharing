import {HttpInterceptorFn} from '@angular/common/http';
import {catchError, throwError} from 'rxjs';
import {ErrorInfo} from '../model/ErrorInfo';
import {ExceptionDTO} from '../api';
import {inject} from '@angular/core';
import {Router} from '@angular/router';

function mapCodeToErrorInfo(statusCode: number): ErrorInfo {
  switch (statusCode) {
    case 400:
    case 500:
      return {
        title: 'Allgemeiner Fehler',
        msg: 'Die Anfrage kann nicht verarbeitet werden'
      }
    case 401:
    case 403:
      return {
        title: 'Authentifizierungsfehler',
        msg: 'Logen Sie sich erneut ein und verwenden Sie den korrekten Usernamen und Passwort'
      }
    case 409:
      return {
        title: 'Konflikt',
        msg: 'Ihre Eingaben konnten nicht verarbeitet werden'
      }
    default:
      return {
        title: 'Unerwarteter Fehler',
        msg: 'Prüfen Sie die Logs'
      }
  }
}

function isExceptionDTO(obj: any): obj is ExceptionDTO {
  return typeof obj === 'object' && obj !== null && 'httpStatus' in obj && 'message' in obj
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router)

  return next(req).pipe(
    catchError(errorResponse => {
      const errorResponseObj = errorResponse.error
      const errorInfo = mapCodeToErrorInfo(errorResponse.status)
      if (isExceptionDTO(errorResponseObj)) {
        errorInfo.msg += ` - ${errorResponseObj.message}`
      }
      if ((errorResponse.status === 401 || errorResponse.status === 403) && !req.url.includes('/api/auth/login')) {
        router.navigate(['/login'])
      }
      if (errorResponse.status >= 500) {
        router.navigate(['/error'])
      }
      return throwError(() => errorInfo)
    })
  );
};
