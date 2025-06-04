import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SelectedDayReservationsComponent} from './selected-day-reservations.component';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {By} from '@angular/platform-browser';

describe('SelectedDayReservationsComponent', () => {
  let component: SelectedDayReservationsComponent;
  let fixture: ComponentFixture<SelectedDayReservationsComponent>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>
  const mockedUser = {username: 'loggedUser'}


  beforeEach(async () => {
    activatedRoute = jasmine.createSpyObj('ActivatedRoute', [], {data: of({user: mockedUser})})

    await TestBed.configureTestingModule({
      imports: [SelectedDayReservationsComponent],
      providers: [
        {provide: ActivatedRoute, useValue: activatedRoute},
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(SelectedDayReservationsComponent);
    fixture.componentRef.setInput('reservationsForSelectedDay', [])
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
    fixture.componentRef.setInput('reservationsForSelectedDay', [{userDTO: mockedUser}, {userDTO: {username: 'notLoggedUser'}}])
    fixture.detectChanges()
    const reservations = fixture.debugElement.queryAll(By.css('.reservation-container'))
    const canEdit = fixture.debugElement.queryAll(By.css('.edit-icon'))

    expect(reservations.length).toBe(2)
    expect(canEdit.length).toBe(1)
  })
});
