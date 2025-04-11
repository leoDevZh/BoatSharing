import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {ToastyComponent} from './toasty.component';
import {Toasty, ToastyService} from '../services/toasty/toasty.service';
import {By} from '@angular/platform-browser';
import {signal} from '@angular/core';

describe('ToastyComponent', () => {
  let component: ToastyComponent;
  let fixture: ComponentFixture<ToastyComponent>;
  let toastyService: ToastyService

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ToastyComponent],
      providers: [ToastyService]
    })
      .compileComponents();

    toastyService = TestBed.inject(ToastyService)
    fixture = TestBed.createComponent(ToastyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should by default have no toasty', () => {
    const getNotificationsSpy = spyOn(toastyService, 'getNotifications').and.returnValue(signal([]))
    fixture = TestBed.createComponent(ToastyComponent)
    component = fixture.componentInstance
    fixture.detectChanges()

    const toasty = fixture.debugElement.queryAll(By.css('.toasty'))

    expect(getNotificationsSpy).toHaveBeenCalledTimes(1)
    expect(toasty.length).toBe(0)
  })

  it('should display have toasty', () => {
    const mockToasties = signal<Toasty[]>([
      {id: '1', type: 'NOTIFICATION', message: 'Info message'},
      {id: '2', type: 'ERROR', message: 'Error message'}
    ])
    const getNotificationsSpy = spyOn(toastyService, 'getNotifications').and.returnValue(mockToasties)
    fixture = TestBed.createComponent(ToastyComponent)
    component = fixture.componentInstance
    fixture.detectChanges()

    const toasty = fixture.debugElement.queryAll(By.css('.toasty'))

    expect(getNotificationsSpy).toHaveBeenCalledTimes(1)
    expect(toasty.length).toBe(2)
  })

  it('should remove toasty on swipeRight', fakeAsync(() => {
    const mockToasties = signal<Toasty[]>([
      {id: '1', type: 'NOTIFICATION', message: 'Info message'}
    ])
    spyOn(toastyService, 'getNotifications').and.returnValue(mockToasties)
    const removeSpy = spyOn(toastyService, 'removeNotification')
    fixture = TestBed.createComponent(ToastyComponent)
    component = fixture.componentInstance
    fixture.detectChanges()

    let messageElement = fixture.debugElement.query(By.css('.toasty'))
    messageElement.triggerEventHandler('swiperight', null)
    fixture.detectChanges()

    expect(component.exitingToasties.has('1')).toBeTruthy()
    expect(messageElement.nativeElement.classList).toContain('exit')

    tick(300)

    expect(removeSpy).toHaveBeenCalledOnceWith('1')
    expect(component.exitingToasties.has('1')).toBeFalse()
  }))

});
