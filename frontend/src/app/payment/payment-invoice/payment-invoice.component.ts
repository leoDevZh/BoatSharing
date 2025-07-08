import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {BoatId, FuelInvoiceDTO, ReadPaymentControllerService, WritePaymentControllerService} from '../../api';
import {DateTime} from 'luxon';
import {ButtonDirective} from '../../shared/button/button.directive';
import {catchError, throwError} from 'rxjs';
import {ErrorInfo} from '../../model/ErrorInfo';
import {ToastyService} from '../../services/toasty/toasty.service';

@Component({
  selector: 'app-payment-invoice',
  imports: [
    ButtonDirective
  ],
  templateUrl: './payment-invoice.component.html',
  styleUrl: './payment-invoice.component.css'
})
export class PaymentInvoiceComponent implements OnInit {
  private activatedRoute = inject(ActivatedRoute);
  private readPaymentService = inject(ReadPaymentControllerService)
  private writePaymentService = inject(WritePaymentControllerService)
  private toastyService = inject(ToastyService)

  private boatId!: BoatId
  protected from: DateTime | null = null
  protected to: DateTime | null = null
  protected fuelInvoiceDTO: FuelInvoiceDTO | null = null

  ngOnInit() {
    this.activatedRoute.data.subscribe(({boatId}) => {
      this.boatId = boatId
      this.loadInvoicePeriod(boatId);
    })
  }

  private loadInvoicePeriod(boatId: BoatId) {
    this.readPaymentService.getNextInvoicePeriod(boatId.value?.toString()!).subscribe(response => {
      this.from = DateTime.fromISO(response.startDate!)
      this.to = DateTime.fromISO(response.endDate!)
    })
  }

  createInvoice() {
    this.writePaymentService.createInvoice(this.boatId.value?.toString()!)
      .pipe(
        catchError((err: ErrorInfo) => {
          this.toastyService.addErrorNotification(`Fehler beim erstellen der Abrechnung: ${err.msg}`)
          return throwError(() => err)
        })
      )
      .subscribe(response => {
        this.fuelInvoiceDTO = response
        this.toastyService.addInfoNotification('Abrechnung erfolgreich erstellt')
      })
  }
}
