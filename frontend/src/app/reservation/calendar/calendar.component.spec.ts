import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CalendarComponent} from './calendar.component';
import {ReservationService} from '../../services/reservation/reservation.service';
import {DateTime} from 'luxon';
import {ReservationUserDTO} from '../../api';
import {signal} from '@angular/core';
import {By} from '@angular/platform-browser';

describe('CalendarComponent', () => {
  let component: CalendarComponent;
  let fixture: ComponentFixture<CalendarComponent>;
  let reservationService: jasmine.SpyObj<ReservationService>


  beforeEach(async () => {
    reservationService = jasmine.createSpyObj('ReservationService', ['findReservationsForDay'])
    await TestBed.configureTestingModule({
      imports: [CalendarComponent],
      providers: [
        {provide: ReservationService, useValue: reservationService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CalendarComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.componentRef.setInput('daysOfMonth', [])
    fixture.detectChanges()
    expect(component).toBeTruthy()
  });

  it('should render correct days and set classes', () => {
    const expectedFirstDayOfMonth = DateTime.fromISO('2025-04-14').startOf('month')
    const expectedDaysOfMonth = [expectedFirstDayOfMonth, DateTime.fromISO('2025-04-14'), DateTime.now(), expectedFirstDayOfMonth.plus({month: 1})]
    const mockedReservations: ReservationUserDTO[] = []
    reservationService.findReservationsForDay.and.returnValue(signal(mockedReservations))
    fixture.componentRef.setInput('daysOfMonth', expectedDaysOfMonth)
    fixture.detectChanges()
    const dayCells = fixture.debugElement.queryAll(By.css('.cell'))
    const reserved = fixture.debugElement.queryAll(By.css('.reserved'))

    expect(dayCells.length).toBe(expectedDaysOfMonth.length + 7) // plus weekdays
    expect(dayCells.at(7)?.nativeElement.classList).toContain('current-month')
    expect(dayCells.at(8)?.nativeElement.classList).toContain('current-month')
    expect(dayCells.at(9)?.nativeElement.classList).toContain('current-day')
    expect(dayCells.at(10)?.nativeElement.classList).not.toContain('current-month')
    expect(reserved.length).toBe(0)
  })

  it('should render reserved status indication correctly', () => {
    const expectedFirstDayOfMonth = DateTime.fromISO('2025-04-14').startOf('month')
    const expectedDaysOfMonth = [expectedFirstDayOfMonth, expectedFirstDayOfMonth.plus({day: 1})]
    const mockedReservationsDayOne: ReservationUserDTO[] = [{
      startDateTime: '2025-04-14T08:00:00',
      endDateTime: '2025-04-14T12:00:00'
    }]
    const mockedReservationsDayTwo: ReservationUserDTO[] = [{
      startDateTime: '2025-04-15T13:00:00',
      endDateTime: '2025-04-15T19:00:00'
    }]
    reservationService.findReservationsForDay.withArgs(expectedDaysOfMonth.at(0)!).and.returnValue(signal(mockedReservationsDayOne))
    reservationService.findReservationsForDay.withArgs(expectedDaysOfMonth.at(1)!).and.returnValue(signal(mockedReservationsDayTwo))
    fixture.componentRef.setInput('daysOfMonth', expectedDaysOfMonth)
    fixture.detectChanges()
    const reserved = fixture.debugElement.queryAll(By.css('.reserved'))
    const availabilityContainers = fixture.debugElement.queryAll(By.css('.availability-container'))
    const firstAvailabilitySpans = availabilityContainers[0].queryAll(By.css('span'))
    const secondAvailabilitySpans = availabilityContainers[1].queryAll(By.css('span'));

    expect(reserved.length).toBe(3)
    expect(firstAvailabilitySpans[0].nativeElement.classList).toContain('reserved')
    expect(firstAvailabilitySpans[1].nativeElement.classList).not.toContain('reserved')
    expect(firstAvailabilitySpans[2].nativeElement.classList).not.toContain('reserved')
    expect(secondAvailabilitySpans[0].nativeElement.classList).not.toContain('reserved')
    expect(secondAvailabilitySpans[1].nativeElement.classList).toContain('reserved')
    expect(secondAvailabilitySpans[2].nativeElement.classList).toContain('reserved')
  })

  it('should emit selectedDay on click', () => {
    const expectedFirstDayOfMonth = DateTime.fromISO('2025-04-14').startOf('month')
    const expectedDaysOfMonth = [expectedFirstDayOfMonth, expectedFirstDayOfMonth.plus({day: 1})]
    fixture.componentRef.setInput('daysOfMonth', expectedDaysOfMonth)
    reservationService.findReservationsForDay.and.returnValue(signal([]))
    const emitSelectedDaySpy = jasmine.createSpy('emitSelectedDaySpy')
    component.selectedDayChanged.subscribe(emitSelectedDaySpy)
    fixture.detectChanges()
    const cells = fixture.debugElement.queryAll(By.css('.cell'))

    expect(component.selectedDay()).toBeUndefined()

    cells.at(7)?.nativeElement.click()
    fixture.detectChanges()

    expect(component.selectedDay()).toEqual(expectedDaysOfMonth.at(0))
    expect(emitSelectedDaySpy).toHaveBeenCalledOnceWith(expectedDaysOfMonth.at(0))
  })
});
