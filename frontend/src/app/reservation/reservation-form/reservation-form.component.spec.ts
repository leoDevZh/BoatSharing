import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ReservationFormComponent} from './reservation-form.component';
import {ActivatedRoute} from '@angular/router';
import {of, throwError} from 'rxjs';
import {BoatId, CreateReservationDTO, ReservationUserDTO, UpdateReservationDTO} from '../../api';
import {DateTime} from 'luxon';
import {ReservationService} from '../../services/reservation/reservation.service';
import {ToastyService} from '../../services/toasty/toasty.service';

describe('ReservationFormComponent', () => {
  let component: ReservationFormComponent;
  let fixture: ComponentFixture<ReservationFormComponent>;
  let activatedRoute: jasmine.NonTypedSpyObj<ActivatedRoute>
  let reservationService: jasmine.NonTypedSpyObj<ReservationService>
  let toastyService: jasmine.NonTypedSpyObj<ToastyService>

  const boatId: BoatId = {value: 1}

  beforeEach(async () => {
    activatedRoute = jasmine.createSpyObj('ActivatedRoute', [], {data: of({boatId})})
    reservationService = jasmine.createSpyObj('ReservationService', ['createReservation', 'updateReservation', 'deleteReservation'])
    toastyService = jasmine.createSpyObj('ToastyService', ['addInfoNotification'])

    await TestBed.configureTestingModule({
      imports: [ReservationFormComponent],
      providers: [
        {provide: ActivatedRoute, useValue: activatedRoute},
        {provide: ReservationService, useValue: reservationService},
        {provide: ToastyService, useValue: toastyService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ReservationFormComponent);
    fixture.componentRef.setInput('selectedDay', DateTime.now())
    fixture.detectChanges()
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should init form to create form', () => {
    const title = fixture.nativeElement.querySelector('h2')
    const dayContainer = fixture.nativeElement.querySelector('.day-container')
    const timeContainer = fixture.nativeElement.querySelector('.time-container')
    const hourContainer = fixture.nativeElement.querySelector('.hour-container')
    const btnSubmit = fixture.nativeElement.querySelector('.btn-submit')
    const btnDelete = fixture.nativeElement.querySelector('.btn-delete')
    const btnCancel = fixture.nativeElement.querySelector('.btn-reset')

    expect(title.innerText).toContain('Erstelle')
    expect(dayContainer.innerText).toContain(`${DateTime.now().weekdayShort}, ${DateTime.now().day} ${DateTime.now().monthShort}`)
    expect(timeContainer).not.toBeNull()
    expect(hourContainer).toBeNull()
    expect(btnSubmit.innerText).toBe('Reservieren')
    expect(btnDelete).toBeNull()
    expect(btnCancel).toBeNull()
  });

  it('should call service on create success', () => {
    reservationService.createReservation.and.returnValue(of({}))
    const startTime = new Date()
    startTime.setHours(9, 0)
    const endTime = new Date()
    endTime.setHours(17, 0)
    const createReservationDTO: CreateReservationDTO = {
      boatId: boatId,
      startDateTime: formatDateToLocalISOString(startTime),
      endDateTime: formatDateToLocalISOString(endTime)
    }
    component.form.get('startDateTime')?.setValue(startTime)
    component.form.get('endDateTime')?.setValue(endTime)
    const btnSubmit = fixture.nativeElement.querySelector('.btn-submit')

    btnSubmit.click()
    fixture.detectChanges()

    expect(component.form.valid).toBeTrue()
    expect(reservationService.createReservation).toHaveBeenCalledWith(createReservationDTO)
    expect(toastyService.addInfoNotification).toHaveBeenCalledWith('Reservation erstellt')
    expect(component.form.get('startDateTime')?.value).toEqual(component.selectedDay().toJSDate())
    expect(component.form.get('endDateTime')?.value).toEqual(component.selectedDay().toJSDate())
  })

  it('should not call service on create failure', () => {
    reservationService.createReservation.and.returnValue(throwError(() => 'Some Error'))
    const btnSubmit = fixture.nativeElement.querySelector('.btn-submit')
    const startTime = new Date()
    startTime.setHours(9, 0)
    const endTime = new Date()
    endTime.setHours(17, 0)
    const createReservationDTO: CreateReservationDTO = {
      boatId: boatId,
      startDateTime: formatDateToLocalISOString(startTime),
      endDateTime: formatDateToLocalISOString(endTime)
    }
    component.form.get('startDateTime')?.setValue(startTime)
    component.form.get('endDateTime')?.setValue(endTime)

    btnSubmit.click()
    fixture.detectChanges()

    expect(reservationService.createReservation).toHaveBeenCalledWith(createReservationDTO)
    expect(toastyService.addInfoNotification).not.toHaveBeenCalled()
    expect(component.form.get('startDateTime')?.value).toEqual(startTime)
    expect(component.form.get('endDateTime')?.value).toEqual(endTime)
  })

  it('should init form to update', () => {
    const reservationMock: ReservationUserDTO = {
      reservationId: {value: 1},
      boatId: {value: 1},
      startDateTime: DateTime.now().toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      endDateTime: DateTime.now().plus({hour: 4}).toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      boatHoursOnStart: 15,
      boatHoursOnEnd: 17
    }
    fixture.componentRef.setInput('reservationToUpdate', reservationMock)
    fixture.detectChanges()
    const title = fixture.nativeElement.querySelector('h2')
    const dayContainer = fixture.nativeElement.querySelector('.day-container')
    const timeContainer = fixture.nativeElement.querySelector('.time-container')
    const hourContainer = fixture.nativeElement.querySelector('.hour-container')
    const btnSubmit = fixture.nativeElement.querySelector('.btn-submit')
    const btnDelete = fixture.nativeElement.querySelector('.btn-delete')
    const btnCancel = fixture.nativeElement.querySelector('.btn-reset')

    expect(title.innerText).toContain('Update')
    expect(dayContainer.innerText).toContain(`${DateTime.now().weekdayShort}, ${DateTime.now().day} ${DateTime.now().monthShort}`)
    expect(timeContainer).not.toBeNull()
    expect(hourContainer).not.toBeNull()
    expect(btnSubmit.innerText).toBe('Update')
    expect(btnDelete.innerText).toBe('Löschen')
    expect(btnCancel.innerText).toBe('Abbrechen')
    expect(component.form.get('startDateTime')?.value).toEqual(new Date(reservationMock.startDateTime!))
    expect(component.form.get('endDateTime')?.value).toEqual(new Date(reservationMock.endDateTime!))
    expect(component.form.get('startHours')?.value).toBe(reservationMock.boatHoursOnStart)
    expect(component.form.get('endHours')?.value).toBe(reservationMock.boatHoursOnEnd)
  })

  it('should call service an reset form on update success', () => {
    reservationService.updateReservation.and.returnValue(of({}))
    spyOn(component.reloadReservations, 'emit')
    const reservationMock: ReservationUserDTO = {
      reservationId: {value: 1},
      boatId: {value: 1},
      startDateTime: DateTime.now().toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      endDateTime: DateTime.now().plus({hour: 4}).toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      boatHoursOnStart: 15,
      boatHoursOnEnd: 17
    }
    const reservationToUpdate: UpdateReservationDTO = {
      reservationId: reservationMock.reservationId!,
      startDateTime: reservationMock.startDateTime!,
      endDateTime: reservationMock.endDateTime!,
      boatEngineHoursOnStart: reservationMock.boatHoursOnStart,
      boatEngineHoursOnEnd: reservationMock.boatHoursOnEnd
    }
    fixture.componentRef.setInput('reservationToUpdate', reservationMock)
    fixture.detectChanges()
    let btnSubmit = fixture.nativeElement.querySelector('.btn-submit')

    btnSubmit.click()
    fixture.detectChanges()

    const title = fixture.nativeElement.querySelector('h2')
    const dayContainer = fixture.nativeElement.querySelector('.day-container')
    const timeContainer = fixture.nativeElement.querySelector('.time-container')
    const hourContainer = fixture.nativeElement.querySelector('.hour-container')
    const btnDelete = fixture.nativeElement.querySelector('.btn-delete')
    const btnCancel = fixture.nativeElement.querySelector('.btn-reset')
    btnSubmit = fixture.nativeElement.querySelector('.btn-submit')

    expect(reservationService.updateReservation).toHaveBeenCalledWith(reservationToUpdate)
    expect(toastyService.addInfoNotification).toHaveBeenCalledWith('Reservation updated')
    expect(component.form.get('startDateTime')?.value).toEqual(component.selectedDay().toJSDate())
    expect(component.form.get('endDateTime')?.value).toEqual(component.selectedDay().toJSDate())
    expect(title.innerText).toContain('Erstelle')
    expect(dayContainer.innerText).toContain(`${DateTime.now().weekdayShort}, ${DateTime.now().day} ${DateTime.now().monthShort}`)
    expect(timeContainer).not.toBeNull()
    expect(hourContainer).toBeNull()
    expect(btnSubmit.innerText).toBe('Reservieren')
    expect(btnDelete).toBeNull()
    expect(btnCancel).toBeNull()
    expect(component.reloadReservations.emit).toHaveBeenCalledTimes(1)
  })

  it('should call service an reset form on update failure', () => {
    reservationService.updateReservation.and.returnValue(throwError(() => 'Some Error'))
    spyOn(component.reloadReservations, 'emit')
    const reservationMock: ReservationUserDTO = {
      reservationId: {value: 1},
      boatId: {value: 1},
      startDateTime: DateTime.now().toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      endDateTime: DateTime.now().plus({hour: 4}).toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      boatHoursOnStart: 15,
      boatHoursOnEnd: 17
    }
    const reservationToUpdate: UpdateReservationDTO = {
      reservationId: reservationMock.reservationId!,
      startDateTime: reservationMock.startDateTime!,
      endDateTime: reservationMock.endDateTime!,
      boatEngineHoursOnStart: reservationMock.boatHoursOnStart,
      boatEngineHoursOnEnd: reservationMock.boatHoursOnEnd
    }
    fixture.componentRef.setInput('reservationToUpdate', reservationMock)
    fixture.detectChanges()
    const btnSubmit = fixture.nativeElement.querySelector('.btn-submit')

    btnSubmit.click()
    fixture.detectChanges()

    expect(reservationService.updateReservation).toHaveBeenCalledWith(reservationToUpdate)
    expect(toastyService.addInfoNotification).not.toHaveBeenCalled()
    expect(component.reloadReservations.emit).not.toHaveBeenCalled()
  })

  it('should call service an reset form on delete success', () => {
    reservationService.deleteReservation.and.returnValue(of({}))
    spyOn(component.reloadReservations, 'emit')
    const reservationMock: ReservationUserDTO = {
      reservationId: {value: 1},
      boatId: {value: 1},
      startDateTime: DateTime.now().toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      endDateTime: DateTime.now().plus({hour: 4}).toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      boatHoursOnStart: 15,
      boatHoursOnEnd: 17
    }
    fixture.componentRef.setInput('reservationToUpdate', reservationMock)
    fixture.detectChanges()
    let btnDelete = fixture.nativeElement.querySelector('.btn-delete')

    btnDelete.click()
    fixture.detectChanges()

    const title = fixture.nativeElement.querySelector('h2')
    const dayContainer = fixture.nativeElement.querySelector('.day-container')
    const timeContainer = fixture.nativeElement.querySelector('.time-container')
    const hourContainer = fixture.nativeElement.querySelector('.hour-container')
    const btnSubmit = fixture.nativeElement.querySelector('.btn-submit')
    const btnCancel = fixture.nativeElement.querySelector('.btn-reset')
    btnDelete = fixture.nativeElement.querySelector('.btn-delete')

    expect(reservationService.deleteReservation).toHaveBeenCalledWith(reservationMock.reservationId)
    expect(toastyService.addInfoNotification).toHaveBeenCalledWith('Reservation gelöscht')
    expect(component.form.get('startDateTime')?.value).toEqual(component.selectedDay().toJSDate())
    expect(component.form.get('endDateTime')?.value).toEqual(component.selectedDay().toJSDate())
    expect(title.innerText).toContain('Erstelle')
    expect(dayContainer.innerText).toContain(`${DateTime.now().weekdayShort}, ${DateTime.now().day} ${DateTime.now().monthShort}`)
    expect(timeContainer).not.toBeNull()
    expect(hourContainer).toBeNull()
    expect(btnSubmit.innerText).toBe('Reservieren')
    expect(btnDelete).toBeNull()
    expect(btnCancel).toBeNull()
    expect(component.reloadReservations.emit).toHaveBeenCalledTimes(1)
  })

  it('should call service an reset form on delete failure', () => {
    reservationService.deleteReservation.and.returnValue(throwError(() => 'Some error'))
    spyOn(component.reloadReservations, 'emit')
    const reservationMock: ReservationUserDTO = {
      reservationId: {value: 1},
      boatId: {value: 1},
      startDateTime: DateTime.now().toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      endDateTime: DateTime.now().plus({hour: 4}).toFormat("yyyy-MM-dd'T'HH:mm").toString(),
      boatHoursOnStart: 15,
      boatHoursOnEnd: 17
    }
    fixture.componentRef.setInput('reservationToUpdate', reservationMock)
    fixture.detectChanges()
    const btnDelete = fixture.nativeElement.querySelector('.btn-delete')

    btnDelete.click()
    fixture.detectChanges()

    expect(reservationService.deleteReservation).toHaveBeenCalledWith(reservationMock.reservationId)
    expect(toastyService.addInfoNotification).not.toHaveBeenCalled()
    expect(component.reloadReservations.emit).not.toHaveBeenCalled()
  })
});

function formatDateToLocalISOString(date: Date) {
  const pad = (n: number) => n.toString().padStart(2, '0')

  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hours = pad(date.getHours())
  const minutes = pad(date.getMinutes())

  return `${year}-${month}-${day}T${hours}:${minutes}`
}
