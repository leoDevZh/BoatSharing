import {Component, inject, OnInit} from '@angular/core';
import {ReadPaymentControllerService, UserDTO} from '../api';
import {catchError, finalize, forkJoin, Observable, throwError} from 'rxjs';
import {ToastyService} from '../services/toasty/toasty.service';
import {ErrorInfo} from '../model/ErrorInfo';
import {MatIcon} from '@angular/material/icon';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {OpenDebtComponent} from './open-debt/open-debt.component';
import {PaybackCheckComponent} from './payback-check/payback-check.component';
import {NgComponentOutlet} from '@angular/common';
import {PaymentListComponent} from './payment-list/payment-list.component';
import {PaymentInvoiceComponent} from './payment-invoice/payment-invoice.component';
import {ActivatedRoute} from '@angular/router';

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
  protected isLoading: boolean = false

  private readPaymentService = inject(ReadPaymentControllerService)
  private toastyService = inject(ToastyService)
  private activatedRoute = inject(ActivatedRoute);

  user!: UserDTO;

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({user}) => {
      this.user = user
      this.loadData();
    })
  }

  private loadData() {
    if (this.user?.username !== 'Harry') {
      this.isLoading = true
      forkJoin({
        paybacks: this.readPaymentService.getAllDebtsToCheck(0),
        openPayments: this.readPaymentService.getAllOpenDebtsFromLoggedInUser(0)
      })
        .pipe(
          catchError(this.handleError),
          finalize(() => this.isLoading = false)
        )
        .subscribe(({ paybacks, openPayments }) => {
          this.paybacksToCheckTotalNumber = paybacks.totalNumber!;
          this.openPaymentsTotalNumber = openPayments.totalNumber!;
        });

    }
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
