import {Component, inject, input, output, signal} from '@angular/core';
import {ReservationService} from '../../services/reservation/reservation.service';
import {DateTime, Info} from 'luxon';

const weekdays = Info.weekdays('short', {locale: 'de'})

export enum ReservedTime {
  MORNING = "07:00",
  AFTERNOON = "13:00",
  EVENING = " 18:00",
}

@Component({
  selector: 'bs-calendar',
  imports: [],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.css'
})

export class CalendarComponent {
  daysOfMonth = input.required<DateTime<true>[]>()
  selectedDayChanged = output<DateTime>()

  selectedDay = signal<DateTime | undefined>(undefined)

  private reservationService = inject(ReservationService)

  protected readonly weekdays = weekdays;
  protected readonly ReservedTime = ReservedTime;

  isCurrentDay(day: DateTime): boolean {
    return day.startOf('day').equals(DateTime.now().startOf('day'))
  }

  isCurrentMonth(day: DateTime): boolean {
    return day.month === this.daysOfMonth().at(0)?.endOf('week').month
  }

  isSelectedDay(day: DateTime): boolean {
    return this.selectedDay() !== undefined ? day.startOf('day').equals(this.selectedDay()!.startOf('day')) : false
  }

  isReserved(day: DateTime, reservedTime: ReservedTime): boolean {
    switch (reservedTime) {
      case ReservedTime.MORNING:
        return this.reservationService.findReservationsForDay(day)()
          .filter(reservationUserDTO => reservationUserDTO.startDateTime !== undefined && reservationUserDTO.endDateTime !== undefined)
          .some(reservationUserDTO => {
            const startHour = DateTime.fromISO(reservationUserDTO.startDateTime!).hour
            const endHour = DateTime.fromISO(reservationUserDTO.endDateTime!).hour
            return (startHour >= 7 && startHour < 13) || endHour <= 13
          })
      case ReservedTime.AFTERNOON:
        return this.reservationService.findReservationsForDay(day)()
          .filter(reservationUserDTO => reservationUserDTO.startDateTime !== undefined && reservationUserDTO.endDateTime !== undefined)
          .some(reservationUserDTO => {
            const startHour = DateTime.fromISO(reservationUserDTO.startDateTime!).hour
            const endHour = DateTime.fromISO(reservationUserDTO.endDateTime!).hour
            return (startHour >= 13 && startHour < 18) || (endHour > 13 && endHour <= 18)
          })
      case ReservedTime.EVENING:
        return this.reservationService.findReservationsForDay(day)()
          .filter(reservationUserDTO => reservationUserDTO.startDateTime !== undefined && reservationUserDTO.endDateTime !== undefined)
          .some(reservationUserDTO => {
            const startHour = DateTime.fromISO(reservationUserDTO.startDateTime!).hour
            const endHour = DateTime.fromISO(reservationUserDTO.endDateTime!).hour
            return startHour >= 18 || endHour > 18
          })
      default:
        return false
    }
  }

  selectDay(day: DateTime<true>) {
    this.selectedDay.set(day)
    this.selectedDayChanged.emit(day)
  }
}
