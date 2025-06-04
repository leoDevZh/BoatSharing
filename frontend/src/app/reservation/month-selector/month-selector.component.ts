import {Component, model} from '@angular/core';
import {DateTime} from 'luxon';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'bs-month-selector',
  imports: [
    MatIcon
  ],
  templateUrl: './month-selector.component.html',
  styleUrl: './month-selector.component.css'
})
export class MonthSelectorComponent {
  month = model.required<DateTime>()

  changeMonth(add: number): void {
    this.month.update(currentMonth => currentMonth.plus({month: add}))
  }
}
