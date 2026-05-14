import {Component, inject, OnInit} from '@angular/core';
import {
  DebtUserDTO,
  PagedResultListPaymentUserDTO,
  PaymentId,
  PaymentUserDTO,
  ReadPaymentControllerService,
  UserId,
  WritePaymentControllerService
} from '../../api';
import {catchError, throwError} from 'rxjs';
import {ErrorInfo} from '../../model/ErrorInfo';
import {ToastyService} from '../../services/toasty/toasty.service';
import {MatIcon} from '@angular/material/icon';
import {DateTime} from 'luxon';
import {PaymentFormComponent} from './payment-form/payment-form.component';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'bs-payment-list',
  imports: [
    MatIcon,
    PaymentFormComponent
  ],
  templateUrl: './payment-list.component.html',
  styleUrl: './payment-list.component.css'
})
export class PaymentListComponent implements OnInit {
  protected readonly DateTime = DateTime;
  private readPaymentService = inject(ReadPaymentControllerService)
  private toastyService = inject(ToastyService)
  private writePaymentService = inject(WritePaymentControllerService)
  private activatedRoute = inject(ActivatedRoute);

  private currentPage = 0
  protected paymentUserDTOs: PagedResultListPaymentUserDTO | null = null
  protected extendIdx: number | null = null
  protected loggedInUserId!: UserId

  constructor() {
    this.loadData();
  }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({user}) => {
      this.loggedInUserId = user.userId
    })
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

  isNewYear(index: number): boolean {
    if (index === 0) return false;
    const current = DateTime.fromISO(this.paymentUserDTOs?.result?.[index].payedAt!).year;
    const previous = DateTime.fromISO(this.paymentUserDTOs?.result?.[index - 1].payedAt!).year;
    return current !== previous;
  }

  deletePayment(paymentId: PaymentId): void {
    this.writePaymentService.deletePayment(paymentId.value?.toString()!)
      .pipe(catchError((err: ErrorInfo) => {
        this.toastyService.addErrorNotification(`Fehler beim löschen der Zahlung: ${err.msg}`)
        return throwError(() => err)
      }))
      .subscribe(res => {
        this.toastyService.addInfoNotification('Zahlung erfolgreich gelöscht')
        this.loadData()
      })
  }
}
