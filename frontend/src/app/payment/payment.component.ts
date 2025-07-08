import {Component, inject, OnInit} from '@angular/core';
import {ReadPaymentControllerService} from '../api';
import {catchError, Observable, throwError} from 'rxjs';
import {ToastyService} from '../services/toasty/toasty.service';
import {ErrorInfo} from '../model/ErrorInfo';
import {MatIcon} from '@angular/material/icon';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {OpenDebtComponent} from './open-debt/open-debt.component';
import {PaybackCheckComponent} from './payback-check/payback-check.component';
import {NgComponentOutlet} from '@angular/common';
import {PaymentListComponent} from './payment-list/payment-list.component';
import {PaymentInvoiceComponent} from './payment-invoice/payment-invoice.component';

export type PaymentSideComponent = 'ZAHLUNG' | 'OFFENE' | 'RUECK' | 'INVOICE'

@Component({
  selector: 'bs-payment',
  imports: [
    MatIcon,
    MatProgressSpinner,
    NgComponentOutlet
  ],
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.css'
})
export class PaymentComponent implements OnInit {

  private activePaymentComponent: PaymentSideComponent = 'ZAHLUNG'
  protected displaySideContainer: boolean = false
  protected paybacksToCheckTotalNumber: number | null = null
  protected openPaymentsTotalNumber: number | null = null

  private readPaymentService = inject(ReadPaymentControllerService)
  private toastyService = inject(ToastyService)

  ngOnInit(): void {
    this.loadData();
  }

  private loadData() {
    this.readPaymentService.getAllDebtsToCheck(0)
      .pipe(catchError(this.handleError))
      .subscribe(response => this.paybacksToCheckTotalNumber = response.totalNumber!)

    this.readPaymentService.getAllOpenDebtsFromLoggedInUser(0)
      .pipe(catchError(this.handleError))
      .subscribe(response => this.openPaymentsTotalNumber = response.totalNumber!)
  }

  slideBack(): void {
    this.displaySideContainer = false
    this.loadData()
  }

  setActivePaymentComponent(activePaymentComponent: PaymentSideComponent): void {
    this.activePaymentComponent = activePaymentComponent
    this.displaySideContainer = true
  }

  get activeComponent(): any {
    switch (this.activePaymentComponent) {
      case "ZAHLUNG":
        return PaymentListComponent
      case "OFFENE":
        return OpenDebtComponent
      case "RUECK":
        return PaybackCheckComponent
      case 'INVOICE':
        return PaymentInvoiceComponent
      default:
        return
    }
  }

  private handleError = (err: ErrorInfo): Observable<never> => {
    this.toastyService.addErrorNotification(`Fehler beim laden der Daten: ${err.msg}`)
    return throwError(() => err)
  }
  protected readonly OpenDebtComponent = OpenDebtComponent;
}
