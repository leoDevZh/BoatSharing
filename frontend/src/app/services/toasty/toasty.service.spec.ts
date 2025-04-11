import {TestBed} from '@angular/core/testing';

import {ToastyService} from './toasty.service';

describe('ToastyService', () => {
  let service: ToastyService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastyService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.getNotifications().length).toBe(0)
  });

  it('should add an error notification', () => {
    const message = 'Test Message'
    const type = 'ERROR'
    service.addErrorNotification(message);

    const notifications = service.getNotifications()();
    expect(notifications.length).toBe(1);
    expect(notifications[0].message).toBe(message);
    expect(notifications[0].type).toBe(type)
  });


  it('should add an Info notification', () => {
    const message = 'Test Message'
    const type = 'NOTIFICATION'
    service.addInfoNotification(message);

    const notifications = service.getNotifications()();
    expect(notifications.length).toBe(1);
    expect(notifications[0].message).toBe(message);
    expect(notifications[0].type).toBe(type)
  });

  it('should remove a notification by index', () => {
    service.addErrorNotification('Test Message');
    service.addErrorNotification('Second Toast');

    let notifications = service.getNotifications()();
    expect(notifications.length).toBe(2);

    service.removeNotification(notifications[0].id);
    notifications = service.getNotifications()();
    expect(notifications.length).toBe(1);
    expect(notifications[0].message).toBe('Second Toast');
  });

  it('should return a readonly signal', () => {
    const readonlySignal = service.getNotifications();
    expect(() => {
      // @ts-expect-error: should not allow mutation
      readonlySignal.set([]);
    }).toThrow();
  });
});
