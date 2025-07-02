import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OpenDebtComponent} from './open-debt.component';
import {PagedResultListDebtPaymentDTO, ReadPaymentControllerService, WritePaymentControllerService} from '../../api';
import {ToastyService} from '../../services/toasty/toasty.service';
import {of} from 'rxjs';

describe('OpenDebtComponent', () => {
  let component: OpenDebtComponent;
  let fixture: ComponentFixture<OpenDebtComponent>;
  let writePaymentControllerService: jasmine.NonTypedSpyObj<WritePaymentControllerService>
  let readPaymentControllerService: jasmine.NonTypedSpyObj<ReadPaymentControllerService>
  let toastyNotification: jasmine.NonTypedSpyObj<ToastyService>
  const mockOpenDebts: PagedResultListDebtPaymentDTO = {result: [], hasNext: false, totalNumber: 0}

  beforeEach(async () => {
    writePaymentControllerService = jasmine.createSpyObj('WritePaymentControllerService', ['setDebtToPayed'])
    readPaymentControllerService = jasmine.createSpyObj('ReadPaymentControllerService', ['getAllOpenDebtsFromLoggedInUser'])
    readPaymentControllerService.getAllOpenDebtsFromLoggedInUser.and.returnValue(of(mockOpenDebts))
    toastyNotification = jasmine.createSpyObj('ToastyService', ['addErrorNotification'])

    await TestBed.configureTestingModule({
      imports: [OpenDebtComponent],
      providers: [
        {provide: ToastyService, useValue: toastyNotification},
        {provide: ReadPaymentControllerService, useValue: readPaymentControllerService},
        {provide: WritePaymentControllerService, useValue: writePaymentControllerService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(OpenDebtComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
