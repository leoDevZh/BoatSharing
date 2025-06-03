import {DateTime, Interval} from 'luxon';
import {Component, computed, effect, inject, OnInit, signal} from '@angular/core';
import {MonthSelectorComponent} from './month-selector/month-selector.component';
import {ActivatedRoute} from '@angular/router';
import {BoatId, ReservationUserDTO} from '../api';
import {ReservationService} from '../services/reservation/reservation.service';
import {CalendarComponent} from './calendar/calendar.component';
import {SelectedDayReservationsComponent} from './selected-day-reservations/selected-day-reservations.component';
import {ReservationFormComponent} from './reservation-form/reservation-form.component';

@Component({
  selector: 'bs-reservation',
  imports: [
    MonthSelectorComponent,
    CalendarComponent,
    SelectedDayReservationsComponent,
    ReservationFormComponent
  ],
  templateUrl: './reservation.component.html',
  styleUrl: './reservation.component.css'
})
export class ReservationComponent implements OnInit {

  private activatedRoute = inject(ActivatedRoute);
  private reservationService = inject(ReservationService)

  boatId!: BoatId
  firstDayOfActiveMonth = signal(DateTime.now().startOf('month'))
  daysOfMonth = computed(() => {
    return Interval.fromDateTimes(
      this.firstDayOfActiveMonth().startOf('month').startOf('week'),
      this.firstDayOfActiveMonth().endOf('month').endOf('week')
    )
      .splitBy({day: 1})
      .map(d => d.start)
      .filter(d => d !== null)
  })
  selectedDay = signal<DateTime | undefined>(undefined)
  reservationToUpdate = signal<ReservationUserDTO | undefined>(undefined)

  constructor() {
    effect(() => {
      this.reloadReservations();
    })
  }

  ngOnInit() {
    this.activatedRoute.data.subscribe(({boatId}) => {
      this.boatId = boatId
    })
  }

  onSelectedDayChanged($event: DateTime) {
    this.selectedDay.set($event)
  }

  onUpdateReservation($event: ReservationUserDTO) {
    this.reservationToUpdate.set({...$event})
  }

  onReloadReservations() {
    this.reloadReservations()
  }

  private reloadReservations() {
    if (this.boatId === undefined) return
    const from = this.daysOfMonth().at(0)!
    const to = this.daysOfMonth().at(-1)!
    this.reservationService.findReservationForPeriod(from, to, this.boatId)
  }
}
