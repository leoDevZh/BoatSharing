import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PaymentListComponent} from './payment-list.component';
import {PagedResultListPaymentUserDTO, ReadPaymentControllerService, WritePaymentControllerService} from '../../api';
import {ToastyService} from '../../services/toasty/toasty.service';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {Component, output} from '@angular/core';

@Component({
  selector: 'bs-payment-form',
  template: ''
})
class PaymentFormStubComponent {
  createPayment = output<void>();
}

describe('PaymentListComponent', () => {
  let component: PaymentListComponent;
  let fixture: ComponentFixture<PaymentListComponent>;
  let readPaymentControllerService: jasmine.NonTypedSpyObj<ReadPaymentControllerService>;
  let writePaymentControllerService: jasmine.NonTypedSpyObj<WritePaymentControllerService>;
  let toastyService: jasmine.NonTypedSpyObj<ToastyService>;
  let activatedRoute: jasmine.NonTypedSpyObj<ActivatedRoute>;

  const mockPayments: PagedResultListPaymentUserDTO = {result: [], hasNext: false, totalNumber: 0};

  beforeEach(async () => {
    readPaymentControllerService = jasmine.createSpyObj('ReadPaymentControllerService', ['getAllPayments']);
    writePaymentControllerService = jasmine.createSpyObj('WritePaymentControllerService', ['deletePayment']);
    toastyService = jasmine.createSpyObj('ToastyService', ['addErrorNotification', 'addInfoNotification']);
    activatedRoute = jasmine.createSpyObj('ActivatedRoute', [], {data: of({user: {userId: {value: 1}}})});
    readPaymentControllerService.getAllPayments.and.returnValue(of(mockPayments));

    await TestBed.configureTestingModule({
      imports: [PaymentListComponent],
      providers: [
        {provide: ReadPaymentControllerService, useValue: readPaymentControllerService},
        {provide: WritePaymentControllerService, useValue: writePaymentControllerService},
        {provide: ToastyService, useValue: toastyService},
        {provide: ActivatedRoute, useValue: activatedRoute}
      ]
    })
      .overrideComponent(PaymentListComponent, {
        set: {imports: [PaymentFormStubComponent]}
      })
      .compileComponents();

    fixture = TestBed.createComponent(PaymentListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('isNewYear', () => {
    beforeEach(() => {
      (component as any).paymentUserDTOs = {
        result: [
          {payedAt: '2024-06-15T10:00:00'},
          {payedAt: '2024-11-20T10:00:00'},
          {payedAt: '2025-01-05T10:00:00'},
          {payedAt: '2025-08-10T10:00:00'},
        ],
        hasNext: false,
        totalNumber: 4
      };
    });

    it('should return false when index is 0', () => {
      expect(component.isNewYear(0)).toBeFalse();
    });

    it('should return false when consecutive payments are in the same year', () => {
      expect(component.isNewYear(1)).toBeFalse();
    });

    it('should return true when consecutive payments are in different years', () => {
      expect(component.isNewYear(2)).toBeTrue();
    });

    it('should return false when consecutive payments are in the same year after a year change', () => {
      expect(component.isNewYear(3)).toBeFalse();
    });
  });
});
