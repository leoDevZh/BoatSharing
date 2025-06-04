import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SelectedDayReservationsComponent} from './selected-day-reservations.component';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {ReservationService} from '../../services/reservation/reservation.service';
import {DateTime} from 'luxon';
import {ReservationUserDTO} from '../../api';
import {signal} from '@angular/core';
import {By} from '@angular/platform-browser';

describe('SelectedDayReservationsComponent', () => {
  let component: SelectedDayReservationsComponent;
  let fixture: ComponentFixture<SelectedDayReservationsComponent>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>
  let reservationService: jasmine.NonTypedSpyObj<ReservationService>
  const mockedUser = {username: 'loggedUser'}


  beforeEach(async () => {
    activatedRoute = jasmine.createSpyObj('ActivatedRoute', [], {data: of({user: mockedUser})})
    reservationService = jasmine.createSpyObj('ReservationService', ['findReservationsForDay'])
    reservationService.findReservationsForDay.and.returnValue(signal<ReservationUserDTO[]>([{userDTO: mockedUser}, {userDTO: {username: 'notLoggedUser'}}]))

    await TestBed.configureTestingModule({
      imports: [SelectedDayReservationsComponent],
      providers: [
        {provide: ActivatedRoute, useValue: activatedRoute},
        {provide: ReservationService, useValue: reservationService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(SelectedDayReservationsComponent);
    fixture.componentRef.setInput('selectedDay', DateTime.now())
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load loggedInUsername on Init', () => {
    expect(component.loggedInUsername).toBe(mockedUser.username)

  })

  it('should display edit on reservation belong to logged in user', () => {
    const reservations = fixture.debugElement.queryAll(By.css('.reservation-container'))
    const canEdit = fixture.debugElement.queryAll(By.css('.edit-icon'))

    expect(reservations.length).toBe(2)
    expect(canEdit.length).toBe(1)
  })
});
