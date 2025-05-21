import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ReservationComponent} from './reservation.component';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {BoatId} from '../api';
import {ReservationService} from '../services/reservation/reservation.service';
import {Component, model} from '@angular/core';
import {DateTime} from 'luxon';

@Component({
  selector: 'bs-month-selector',
  template: ''
})
class MonthSelectorStubComponent {
  month = model<DateTime>(DateTime.now().startOf('month'))
}

describe('ReservationComponent', () => {
  let component: ReservationComponent;
  let fixture: ComponentFixture<ReservationComponent>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>
  let reservationService: jasmine.SpyObj<ReservationService>
  const boatId: BoatId = {value: 1}

  beforeEach(async () => {
    reservationService = jasmine.createSpyObj('ReservationService', ['findReservationForPeriod'])
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
          imports: [MonthSelectorStubComponent]
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
    expect(component.firstDayOfActiveMonth().toFormat('yyyy-MM-dd')).toBe(DateTime.now().startOf('month').toFormat('yyyy-MM-dd'))
    expect(component.daysOfMonth().at(0)?.toFormat('yyyy-MM-dd')).toBe(DateTime.now().startOf('month').startOf('week').toFormat('yyyy-MM-dd'))
    expect(component.daysOfMonth().at(-1)?.toFormat('yyyy-MM-dd')).toBe(DateTime.now().endOf('month').endOf('week').toFormat('yyyy-MM-dd'))
  })

  it('should call service on init', () => {
    expect(reservationService.findReservationForPeriod).toHaveBeenCalledWith(component.daysOfMonth().at(0)!, component.daysOfMonth().at(-1)!, component.boatId)
  })
});
