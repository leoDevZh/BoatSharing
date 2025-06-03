import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ReservationComponent} from './reservation.component';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {BoatId, ReservationUserDTO} from '../api';
import {ReservationService} from '../services/reservation/reservation.service';
import {Component, input, model, output} from '@angular/core';
import {DateTime} from 'luxon';
import {By} from '@angular/platform-browser';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'bs-month-selector',
  template: ''
})
class MonthSelectorStubComponent {
  month = model.required<DateTime>()
}

@Component({
  selector: 'bs-calendar',
  template: ''
})
class CalendarStubComponent {
  daysOfMonth = input.required<DateTime<true>[]>()
  selectedDayChanged = output<DateTime>()
}

@Component({
  selector: 'bs-selected-day-reservations',
  template: ''
})
class SelectedDayReservationsStubComponent {
  selectedDay = input.required<DateTime>()
  reservationToUpdateEmit = output<ReservationUserDTO>()
}

@Component({
  selector: 'bs-reservation-form',
  template: ''
})
class ReservationFormStubComponent {
  selectedDay = input.required<DateTime>()
  reservationToUpdate = model<ReservationUserDTO | undefined>(undefined)
  reloadReservations = output<void>()
}

describe('ReservationComponent', () => {
  let component: ReservationComponent;
  let fixture: ComponentFixture<ReservationComponent>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>
  let reservationService: jasmine.SpyObj<ReservationService>
  const boatId: BoatId = {value: 1}

  beforeEach(async () => {
    reservationService = jasmine.createSpyObj('ReservationService', ['findReservationForPeriod', 'findReservationsForDay'])
    activatedRoute = jasmine.createSpyObj('ActivatedRoute', [], {data: of({boatId})})
    await TestBed.configureTestingModule({
      imports: [ReservationComponent],
      providers: [
        {provide: ActivatedRoute, useValue: activatedRoute},
        {provide: ReservationService, useValue: reservationService}
      ]
    })
      .overrideComponent(ReservationComponent, {
        set: {
          imports: [
            MonthSelectorStubComponent,
            CalendarStubComponent,
            SelectedDayReservationsStubComponent,
            ReservationFormStubComponent,
            MatIcon
          ]
        }
      })
      .compileComponents();

    fixture = TestBed.createComponent(ReservationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load boatId on Init', () => {
    expect(component.boatId).toEqual(boatId)
  })

  it('should initialize date signals with current month by default', () => {
    const monthSelectorComponent = fixture.debugElement.query(By.directive(MonthSelectorStubComponent))
    const calendarComponent = fixture.debugElement.query(By.directive(CalendarStubComponent))

    expect(component.firstDayOfActiveMonth().toFormat('yyyy-MM-dd')).toBe(DateTime.now().startOf('month').toFormat('yyyy-MM-dd'))
    expect(component.daysOfMonth().at(0)?.toFormat('yyyy-MM-dd')).toBe(DateTime.now().startOf('month').startOf('week').toFormat('yyyy-MM-dd'))
    expect(component.daysOfMonth().at(-1)?.toFormat('yyyy-MM-dd')).toBe(DateTime.now().endOf('month').endOf('week').toFormat('yyyy-MM-dd'))
    expect(monthSelectorComponent.componentInstance.month()).toEqual(component.firstDayOfActiveMonth())
    expect(calendarComponent.componentInstance.daysOfMonth().at(0)?.toFormat('yyyy-MM-dd')).toBe(DateTime.now().startOf('month').startOf('week').toFormat('yyyy-MM-dd'))
    expect(calendarComponent.componentInstance.daysOfMonth().at(-1)?.toFormat('yyyy-MM-dd')).toBe(DateTime.now().endOf('month').endOf('week').toFormat('yyyy-MM-dd'))
  })

  it('should call service on init', () => {
    expect(reservationService.findReservationForPeriod).toHaveBeenCalledWith(component.daysOfMonth().at(0)!, component.daysOfMonth().at(-1)!, component.boatId)
  })

  it('should change date signals on changing month', () => {
    const month = DateTime.now().plus({month: 1})
    const monthSelector = fixture.debugElement.query(By.directive(MonthSelectorStubComponent))

    monthSelector.componentInstance.month.set(month)
    fixture.detectChanges()

    expect(component.firstDayOfActiveMonth().toFormat('yyyy-MM-dd')).toBe(month.toFormat('yyyy-MM-dd'))
    expect(component.daysOfMonth().at(0)?.toFormat('yyyy-MM-dd')).toBe(month.startOf('month').startOf('week').toFormat('yyyy-MM-dd'))
    expect(component.daysOfMonth().at(-1)?.toFormat('yyyy-MM-dd')).toBe(month.endOf('month').endOf('week').toFormat('yyyy-MM-dd'))
  })

  it('should bind selected day on calender emit selected day', () => {
    const selectedDay = DateTime.fromISO('2025-04-14')
    const calendarComponent = fixture.debugElement.query(By.directive(CalendarStubComponent))

    expect(component.selectedDay()).toBeUndefined()

    calendarComponent.componentInstance.selectedDayChanged.emit(selectedDay)

    expect(component.selectedDay()).toEqual(selectedDay)
  })

  it('should render SelectedDayReservationComponent on selectedDay not undefined', () => {
    let selectedDayReservationsComponent = fixture.debugElement.query(By.directive(SelectedDayReservationsStubComponent))

    expect(selectedDayReservationsComponent).toBeNull()

    component.selectedDay.set(DateTime.now())
    fixture.detectChanges()
    selectedDayReservationsComponent = fixture.debugElement.query(By.directive(SelectedDayReservationsStubComponent))

    expect(selectedDayReservationsComponent.componentInstance.selectedDay()).toEqual(component.selectedDay())
  })

  it('should render SelectedDayReservationComponent on selectedDay not undefined', () => {
    let reservationFormStubComponent = fixture.debugElement.query(By.directive(ReservationFormStubComponent))

    expect(reservationFormStubComponent).toBeNull()

    component.selectedDay.set(DateTime.now())
    fixture.detectChanges()
    reservationFormStubComponent = fixture.debugElement.query(By.directive(SelectedDayReservationsStubComponent))

    expect(reservationFormStubComponent.componentInstance.selectedDay()).toEqual(component.selectedDay())
  })
});
