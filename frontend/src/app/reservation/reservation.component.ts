import {DateTime, Interval} from 'luxon';
import {Component, computed, effect, inject, OnInit, signal} from '@angular/core';
import {MonthSelectorComponent} from './month-selector/month-selector.component';
import {ActivatedRoute} from '@angular/router';
import {BoatId} from '../api';
import {ReservationService} from '../services/reservation/reservation.service';

@Component({
  selector: 'bs-reservation',
  imports: [
    MonthSelectorComponent
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
      this.firstDayOfActiveMonth().startOf('week'),
      this.firstDayOfActiveMonth().endOf('month').endOf('week')
    )
      .splitBy({day: 1})
      .map(d => d.start)
      .filter(d => d !== null)
  })

  constructor() {
    effect(() => {
      if (this.boatId === undefined) return
      const from = this.daysOfMonth().at(0)!
      const to = this.daysOfMonth().at(-1)!
      this.reservationService.findReservationForPeriod(from, to, this.boatId)
    })
  }

  ngOnInit() {
    this.activatedRoute.data.subscribe(({boatId}) => {
      this.boatId = boatId
    })
  }
}
