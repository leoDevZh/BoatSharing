import {computed, inject, Injectable, Signal, signal} from '@angular/core';
import {BoatId, ReadReservationControllerService, ReservationUserDTO} from '../../api';
import {DateTime} from 'luxon';
import {ToastyService} from '../toasty/toasty.service';
import {catchError, map, throwError} from 'rxjs';

const LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
const RESERVATION_CALENDAR_MAP_KEY_FORMAT = "yyyy-MM-dd"

export type ReservationCalendarMap = Map<string, ReservationUserDTO[]>;

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

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
      catchError(err => {
        this.toastyNotification.addErrorNotification('Unerwarteter Fehler: Reservationen konnten nicht geladen werden')
        return throwError(() => err)
      })
    ).subscribe(reservationCalendarMap => this.reservationCalendarMap.set(reservationCalendarMap))
  }

  findReservationsForDay(day: DateTime): Signal<ReservationUserDTO[]> {
    return computed(() => {
      return this.reservationCalendarMap().get(day.toFormat(RESERVATION_CALENDAR_MAP_KEY_FORMAT)) ?? []
    })
  }

  private addReservationUserDTOToMapByDay(reservation: ReservationUserDTO, calendarMap: ReservationCalendarMap) {
    const date = DateTime.fromISO(reservation.startDateTime!).toFormat(RESERVATION_CALENDAR_MAP_KEY_FORMAT)
    if (!calendarMap.has(date)) {
      calendarMap.set(date, [])
    }
    calendarMap.get(date)!.push(reservation)
  }
}
