import {DateTime} from 'luxon';
import {Component, signal} from '@angular/core';
import {MonthSelectorComponent} from './month-selector/month-selector.component';

@Component({
  selector: 'bs-reservation',
  imports: [
    MonthSelectorComponent
  ],
  templateUrl: './reservation.component.html',
  styleUrl: './reservation.component.css'
})
export class ReservationComponent {
  firstDayOfActiveMonth = signal(DateTime.now().startOf('month'))
}
