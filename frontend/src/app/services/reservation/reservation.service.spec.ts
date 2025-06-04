import {TestBed} from '@angular/core/testing';

import {ReservationService} from './reservation.service';
import {ToastyService} from '../toasty/toasty.service';
import {
  CreateReservationDTO,
  ReadReservationControllerService,
  ReservationControllerService,
  ReservationId,
  ReservationUserDTO,
  UpdateReservationDTO
} from '../../api';
import {of, throwError} from 'rxjs';
import {DateTime} from 'luxon';
import {ErrorInfo} from '../../model/ErrorInfo';

describe('ReservationService', () => {
  let service: ReservationService;
  let toastyService: jasmine.SpyObj<ToastyService>
  let readReservationControllerService: jasmine.NonTypedSpyObj<ReadReservationControllerService>
  let writeReservationControllerService: jasmine.NonTypedSpyObj<ReservationControllerService>
  const mockedData: ReservationUserDTO[] = [
    {startDateTime: '2025-05-01T08:00:00'},
    {startDateTime: '2025-05-01T09:00:00'},
    {startDateTime: '2025-05-02T08:00:00'}
  ]

  beforeEach(() => {
    toastyService = jasmine.createSpyObj('ToastyService', ['addErrorNotification'])
    readReservationControllerService = jasmine.createSpyObj('ReadReservationControllerService', ['findReservationForPeriod'])
    writeReservationControllerService = jasmine.createSpyObj('ReservationControllerService', ['cancelReservation', 'create', 'updateReservation'])
    TestBed.configureTestingModule({
      providers: [
        {provide: ToastyService, useValue: toastyService},
        {provide: ReadReservationControllerService, useValue: readReservationControllerService},
        {provide: ReservationControllerService, useValue: writeReservationControllerService}
      ]
    });
    service = TestBed.inject(ReservationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call service on successful findReservationForPeriod', () => {
    const from = DateTime.fromISO('2025-05-01T08:00:00')
    const to = from.plus({day: 1})
    const boatId = {value: 1}
    readReservationControllerService.findReservationForPeriod.and.returnValue(of(mockedData))

    service.findReservationForPeriod(from, to, boatId)
    const res = service.findReservationsForDay(from)

    expect(toastyService.addErrorNotification).not.toHaveBeenCalled()
    expect(readReservationControllerService.findReservationForPeriod).toHaveBeenCalledWith(from.toFormat("yyyy-MM-dd'T'HH:mm:ss"), to.toFormat("yyyy-MM-dd'T'HH:mm:ss"), boatId.value.toString())
    expect(res()).toEqual([mockedData[0], mockedData[1]])
  })

  it('should return empty list on no reservation for day', () => {
    const res = service.findReservationsForDay(DateTime.now())
    expect(res()).toEqual([])
  })

  it('should return reservation for specific day', () => {
    const from = DateTime.fromISO('2025-05-01T08:00:00')
    const to = from.plus({day: 1})
    const boatId = {value: 1}
    readReservationControllerService.findReservationForPeriod.and.returnValue(of(mockedData))
    service.findReservationForPeriod(from, to, boatId)

    const resDayOne = service.findReservationsForDay(from)
    const resDayTwo = service.findReservationsForDay(to)

    expect(resDayTwo()).toEqual([mockedData.at(-1)!])
    expect(resDayOne()).toEqual([mockedData.at(0)!, mockedData.at(1)!])
  })

  it('should call service on cancel successfully', () => {
    const reservationId: ReservationId = {value: 1}
    writeReservationControllerService.cancelReservation.and.returnValue(of())

    service.deleteReservation(reservationId).subscribe(() => {
      expect(writeReservationControllerService.cancelReservation).toHaveBeenCalledWith(reservationId)
    })
  })

  it('should call service on cancel error', () => {
    const reservationId: ReservationId = {value: 1}
    const errorInfo: ErrorInfo = {title: 'Titel', msg: 'Fehlermeldung'}
    writeReservationControllerService.cancelReservation.and.returnValue(throwError(() => errorInfo))

    service.deleteReservation(reservationId).subscribe({
      next: () => fail('Expect error to be thrown'),
      error: err => {
        expect(toastyService.addErrorNotification).toHaveBeenCalledWith(errorInfo.msg)
        expect(err).toEqual(errorInfo)
      }
    })
  })

  it('should create reservation on success', () => {
    const createReservationDTO: CreateReservationDTO = {
      boatId: {value: 1},
      startDateTime: '2025-04-01T13:00',
      endDateTime: '2025-04-01T14:15'
    }
    writeReservationControllerService.create.and.returnValue(of())

    service.createReservation(createReservationDTO).subscribe(() => {
      expect(writeReservationControllerService.create).toHaveBeenCalledWith(createReservationDTO)
    })
  })

  it('should call service on create reservation error', () => {
    const createReservationDTO: CreateReservationDTO = {
      boatId: {value: 1},
      startDateTime: '2025-04-01T13:00',
      endDateTime: '2025-04-01T14:15'
    }
    const errorInfo: ErrorInfo = {title: 'Titel', msg: 'Fehlermeldung'}
    writeReservationControllerService.create.and.returnValue(throwError(() => errorInfo))

    service.createReservation(createReservationDTO).subscribe({
      next: () => fail('Expect error to be thrown'),
      error: err => {
        expect(toastyService.addErrorNotification).toHaveBeenCalledWith(errorInfo.msg)
        expect(err).toEqual(errorInfo)
      }
    })
  })

  it('should update reservation on success', () => {
    const updateReservationDTO: UpdateReservationDTO = {
      reservationId: {value: 1},
      startDateTime: '2025-04-01T13:00',
      endDateTime: '2025-04-01T14:15'
    }
    writeReservationControllerService.updateReservation.and.returnValue(of())

    service.updateReservation(updateReservationDTO).subscribe(() => {
      expect(writeReservationControllerService.updateReservation).toHaveBeenCalledWith(updateReservationDTO)
    })
  })

  it('should call service on update reservation error', () => {
    const updateReservationDTO: UpdateReservationDTO = {
      reservationId: {value: 1},
      startDateTime: '2025-04-01T13:00',
      endDateTime: '2025-04-01T14:15'
    }
    const errorInfo: ErrorInfo = {title: 'titel', msg: 'Fehlermeldung'}
    writeReservationControllerService.updateReservation.and.returnValue(throwError(() => errorInfo))

    service.updateReservation(updateReservationDTO).subscribe({
      next: () => fail('Expect error to be thrown'),
      error: err => {
        expect(toastyService.addErrorNotification).toHaveBeenCalledWith(errorInfo.msg)
        expect(err).toEqual(errorInfo)
      }
    })
  })
});
