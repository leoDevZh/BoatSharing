import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PaybackCheckComponent} from './payback-check.component';
import {ReadPaymentControllerService, WritePaymentControllerService} from '../../api';
import {ToastyService} from '../../services/toasty/toasty.service';
import {of} from 'rxjs';

describe('PaybackCheckComponent', () => {
  let component: PaybackCheckComponent;
  let fixture: ComponentFixture<PaybackCheckComponent>;
  let writePaymentControllerService: jasmine.NonTypedSpyObj<WritePaymentControllerService>
  let readPaymentControllerService: jasmine.NonTypedSpyObj<ReadPaymentControllerService>
  let toastyNotification: jasmine.NonTypedSpyObj<ToastyService>

  beforeEach(async () => {
    writePaymentControllerService = jasmine.createSpyObj('WritePaymentControllerService', ['setDebtToClosed'])
    readPaymentControllerService = jasmine.createSpyObj('ReadPaymentControllerService', ['getAllDebtsToCheck'])
    readPaymentControllerService.getAllDebtsToCheck.and.returnValue(of({}))
    toastyNotification = jasmine.createSpyObj('ToastyService', ['addErrorNotification'])

    await TestBed.configureTestingModule({
      imports: [PaybackCheckComponent],
      providers: [
        {provide: ToastyService, useValue: toastyNotification},
        {provide: ReadPaymentControllerService, useValue: readPaymentControllerService},
        {provide: WritePaymentControllerService, useValue: writePaymentControllerService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PaybackCheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
