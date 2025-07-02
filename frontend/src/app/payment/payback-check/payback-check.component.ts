import {Component, inject, OnInit} from '@angular/core';
import {
  DebtId,
  PagedResultListDebtPaymentDTO,
  ReadPaymentControllerService,
  WritePaymentControllerService
} from '../../api';
import {ToastyService} from '../../services/toasty/toasty.service';
import {ErrorInfo} from '../../model/ErrorInfo';
import {catchError, Observable, throwError} from 'rxjs';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'bs-payback-check',
  imports: [
    MatIcon
  ],
  templateUrl: './payback-check.component.html',
  styleUrl: './payback-check.component.css'
})
export class PaybackCheckComponent implements OnInit {
  protected loading = true
  private activePage = 0
  protected debtsToCheck: PagedResultListDebtPaymentDTO | null = null

  private readPaymentService = inject(ReadPaymentControllerService)
  private writePaymentService = inject(WritePaymentControllerService)
  private toastyService = inject(ToastyService)

  private handleError = (err: ErrorInfo): Observable<never> => {
    this.toastyService.addErrorNotification(`Fehler beim laden der Daten: ${err.msg}`)
    return throwError(() => err)
  }

  ngOnInit(): void {
    this.loadData()
  }

  loadMore() {
    this.loading = true
    this.activePage += 1
    this.loadData()
  }

  private loadData() {
    this.readPaymentService.getAllDebtsToCheck(this.activePage)
      .pipe(catchError(this.handleError))
      .subscribe(response => {
        this.debtsToCheck = {
          ...response,
          result: [...(this.debtsToCheck?.result ? this.debtsToCheck.result : []), ...response.result!]
        }
        this.loading = false
      })
  }

  setDebtToClosed(debtId: DebtId) {
    this.writePaymentService.setDebtToClosed(debtId.value?.toString()!)
      .pipe(catchError(this.handleError))
      .subscribe(res => {
        this.debtsToCheck = {
          ...this.debtsToCheck,
          result: this.debtsToCheck?.result?.filter(d => d.debtUserDTO?.debtId?.value !== debtId.value)
        }

        this.toastyService.addInfoNotification("Offene Rechnung wurde verbucht")
      })
  }
}
