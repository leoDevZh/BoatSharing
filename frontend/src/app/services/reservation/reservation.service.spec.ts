import {TestBed} from '@angular/core/testing';

import {ReservationService} from './reservation.service';
import {ToastyService} from '../toasty/toasty.service';
import {ReadReservationControllerService, ReservationUserDTO} from '../../api';
import {of} from 'rxjs';
import {DateTime} from 'luxon';

describe('ReservationService', () => {
  let service: ReservationService;
  let toastyService: jasmine.SpyObj<ToastyService>
  let readReservationControllerService: jasmine.NonTypedSpyObj<ReadReservationControllerService>
  const mockedData: ReservationUserDTO[] = [
    {startDateTime: '2025-05-01T08:00:00'},
    {startDateTime: '2025-05-01T09:00:00'},
    {startDateTime: '2025-05-02T08:00:00'}
  ]

  beforeEach(() => {
    toastyService = jasmine.createSpyObj('ToastyService', ['addErrorNotification'])
    readReservationControllerService = jasmine.createSpyObj('ReadReservationControllerService', ['findReservationForPeriod'])
    TestBed.configureTestingModule({
      providers: [
        {provide: ToastyService, useValue: toastyService},
        {provide: ReadReservationControllerService, useValue: readReservationControllerService}
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
});
