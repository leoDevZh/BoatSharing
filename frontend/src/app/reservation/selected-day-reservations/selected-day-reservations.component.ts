import {Component, inject, input, OnChanges, OnInit, output, Signal, SimpleChanges} from '@angular/core';
import {ReservationUserDTO} from '../../api';
import {DatePipe} from '@angular/common';
import {ActivatedRoute} from '@angular/router';
import {MatIcon} from '@angular/material/icon';
import {DateTime} from 'luxon';
import {ReservationService} from '../../services/reservation/reservation.service';

@Component({
  selector: 'bs-selected-day-reservations',
  imports: [
    DatePipe,
    MatIcon
  ],
  templateUrl: './selected-day-reservations.component.html',
  styleUrl: './selected-day-reservations.component.css'
})
export class SelectedDayReservationsComponent implements OnInit, OnChanges {

  reservationToUpdateEmit = output<ReservationUserDTO>()
  selectedDay = input.required<DateTime>()

  private activatedRoute = inject(ActivatedRoute);
  private reservationService = inject(ReservationService)

  loggedInUsername!: string
  reservationsForSelectedDay!: Signal<ReservationUserDTO[]>

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({user}) => {
      this.loggedInUsername = user.username
    })
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedDay'].currentValue) {
      this.reservationsForSelectedDay = this.reservationService.findReservationsForDay(this.selectedDay())
    }
  }

  onUpdate(reservation: ReservationUserDTO) {
    this.reservationToUpdateEmit.emit(reservation)
  }
}
