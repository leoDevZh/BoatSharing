import {computed, inject, Injectable, Signal, signal} from '@angular/core';
import {
  BoatId,
  CreateReservationDTO,
  ReadReservationControllerService,
  ReservationControllerService,
  ReservationId,
  ReservationUserDTO,
  UpdateReservationDTO
} from '../../api';
import {DateTime} from 'luxon';
import {ToastyService} from '../toasty/toasty.service';
import {catchError, map, Observable, throwError} from 'rxjs';
import {ErrorInfo} from '../../model/ErrorInfo';

const LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
const RESERVATION_CALENDAR_MAP_KEY_FORMAT = "yyyy-MM-dd"

export type ReservationCalendarMap = Map<string, ReservationUserDTO[]>;

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  private writeReservationControllerService = inject(ReservationControllerService)
  private readReservationControllerService = inject(ReadReservationControllerService)
  private toastyNotification = inject(ToastyService)

  private reservationCalendarMap = signal<ReservationCalendarMap>(new Map())

  findReservationForPeriod(from: DateTime, to: DateTime, boatId: BoatId): void {
    const calendarMap: ReservationCalendarMap = new Map();

    this.readReservationControllerService.findReservationForPeriod(
      from.toFormat(LOCAL_DATE_TIME_FORMAT)!,
      to.toFormat(LOCAL_DATE_TIME_FORMAT)!,
      boatId.value!.toString()
    ).pipe(
      map(reservations => {
        reservations.forEach(reservation => this.addReservationUserDTOToMapByDay(reservation, calendarMap))
        return calendarMap
      }),
      catchError((err: ErrorInfo) => {
        this.toastyNotification.addErrorNotification(err.msg ?? 'Unerwarteter Fehler: Reservationen konnten nicht geladen werden')
        return throwError(() => err)
      })
    ).subscribe(reservationCalendarMap => this.reservationCalendarMap.set(reservationCalendarMap))
  }

  findReservationsForDay(day: DateTime): Signal<ReservationUserDTO[]> {
    return computed(() => {
      return this.reservationCalendarMap().get(day.toFormat(RESERVATION_CALENDAR_MAP_KEY_FORMAT))?.sort(this.sortReservationByStartDate()) ?? []
    })
  }

  deleteReservation(reservationId: ReservationId): Observable<any> {
    return this.writeReservationControllerService.cancelReservation(reservationId.value?.toString()!)
      .pipe(
        catchError((err: ErrorInfo) => {
          this.toastyNotification.addErrorNotification(err.msg ?? 'Unerwarteter Fehler: Reservation konnte nicht gelöscht werden')
          return throwError(() => err)
        })
      )
  }

  createReservation(createReservationDTO: CreateReservationDTO): Observable<any> {
    return this.writeReservationControllerService.create(createReservationDTO)
      .pipe(
        catchError((err: ErrorInfo) => {
          this.toastyNotification.addErrorNotification(err.msg ?? 'Unerwarteter Fehler: Reservation konnte nicht erstellt werden')
          return throwError(() => err)
        })
      )
  }

  updateReservation(updateReservationDTO: UpdateReservationDTO): Observable<any> {
    return this.writeReservationControllerService.updateReservation(updateReservationDTO)
      .pipe(
        catchError((err: ErrorInfo) => {
          this.toastyNotification.addErrorNotification(err.msg ?? 'Unerwarteter Fehler: Reservation konnte nicht updated werden')
          return throwError(() => err)
        })
      )
  }

  private addReservationUserDTOToMapByDay(reservation: ReservationUserDTO, calendarMap: ReservationCalendarMap) {
    const date = DateTime.fromISO(reservation.startDateTime!).toFormat(RESERVATION_CALENDAR_MAP_KEY_FORMAT)
    if (!calendarMap.has(date)) {
      calendarMap.set(date, [])
    }
    calendarMap.get(date)!.push(reservation)
  }

  private sortReservationByStartDate(): (r1: ReservationUserDTO, r2: ReservationUserDTO) => number {
    return (r1, r2) => {
      return DateTime.fromISO(r1.startDateTime!).toMillis() - DateTime.fromISO(r2.startDateTime!).toMillis()
    }
  }
}
