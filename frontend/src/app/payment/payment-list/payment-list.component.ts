import {Component, inject} from '@angular/core';
import {DebtUserDTO, PagedResultListPaymentUserDTO, PaymentUserDTO, ReadPaymentControllerService} from '../../api';
import {catchError, throwError} from 'rxjs';
import {ErrorInfo} from '../../model/ErrorInfo';
import {ToastyService} from '../../services/toasty/toasty.service';
import {MatIcon} from '@angular/material/icon';
import {DateTime} from 'luxon';
import {PaymentFormComponent} from './payment-form/payment-form.component';

@Component({
  selector: 'bs-payment-list',
  imports: [
    MatIcon,
    PaymentFormComponent
  ],
  templateUrl: './payment-list.component.html',
  styleUrl: './payment-list.component.css'
})
export class PaymentListComponent {
  protected readonly DateTime = DateTime;
  private readPaymentService = inject(ReadPaymentControllerService)
  private toastyService = inject(ToastyService)

  private currentPage = 0
  protected paymentUserDTOs: PagedResultListPaymentUserDTO | null = null
  protected extendIdx: number | null = null

  constructor() {
    this.loadData();
  }

  loadMore() {
    this.currentPage += 1
    this.loadData()
  }

  private loadData() {
    this.readPaymentService.getAllPayments(this.currentPage)
      .pipe(catchError((err: ErrorInfo) => {
        this.toastyService.addErrorNotification(`Fehler beim laden der Zahlungen: ${err.msg}`)
        return throwError(() => err)
      }))
      .subscribe(result => {
        this.paymentUserDTOs = result
      })
  }

  getStatusClass(paymentStatus: PaymentUserDTO.PaymentStatusEnum) {
    switch (paymentStatus) {
      case "OPEN":
        return 'status open'
      case "CHARGED":
        return 'status charged'
      case 'CLOSED':
        return 'status closed'
    }
  }

  openIdx(idx: number): void {
    this.extendIdx = idx === this.extendIdx ? null : idx
  }

  getDebtStatusClass(debtStatus: DebtUserDTO.DebtStatusEnum) {
    switch (debtStatus) {
      case "OPEN":
        return 'status open'
      case "PAYED":
        return 'status charged'
      case "CLOSED":
        return 'status closed'
    }
  }

  reloadPayments() {
    this.currentPage = 0
    this.loadData()
  }
}
