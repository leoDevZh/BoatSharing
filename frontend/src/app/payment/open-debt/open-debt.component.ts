import {Component, inject, OnInit} from '@angular/core';
import {
  DebtId,
  PagedResultListDebtPaymentDTO,
  ReadPaymentControllerService,
  WritePaymentControllerService
} from '../../api';
import {ToastyService} from '../../services/toasty/toasty.service';
import {catchError, Observable, throwError} from 'rxjs';
import {ErrorInfo} from '../../model/ErrorInfo';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'bs-open-debt',
  imports: [
    MatIcon
  ],
  templateUrl: './open-debt.component.html',
  styleUrl: './open-debt.component.css'
})
export class OpenDebtComponent implements OnInit {
  protected openDebts: PagedResultListDebtPaymentDTO | null = null
  protected loading = true
  private activePage = 0

  private readPaymentService = inject(ReadPaymentControllerService)
  private writePaymentService = inject(WritePaymentControllerService)
  private toastyService = inject(ToastyService)

  ngOnInit(): void {
    this.loadData();
  }

  private handleError = (err: ErrorInfo): Observable<never> => {
    this.toastyService.addErrorNotification(`Fehler beim laden der Daten: ${err.msg}`)
    return throwError(() => err)
  }

  loadMore() {
    this.loading = true
    this.activePage += 1
    this.loadData()
  }

  private loadData() {
    this.readPaymentService.getAllOpenDebtsFromLoggedInUser(this.activePage)
      .pipe(catchError(this.handleError))
      .subscribe(response => {
        this.openDebts = {
          ...response,
          result: [...(this.openDebts?.result ? this.openDebts.result : []), ...response.result!]
        }
        this.loading = false
      })
  }

  setDebtToPaid(debtId: DebtId) {
    this.writePaymentService.setDebtToPayed(debtId!.value!.toString())
      .pipe(catchError(this.handleError))
      .subscribe(res => {
        this.openDebts = {
          ...this.openDebts,
          result: this.openDebts?.result?.filter(d => d.debtUserDTO?.debtId?.value !== debtId.value)
        }
        this.toastyService.addInfoNotification("Offene Rechnung zur überprüfung verbucht")
      })
  }
}
