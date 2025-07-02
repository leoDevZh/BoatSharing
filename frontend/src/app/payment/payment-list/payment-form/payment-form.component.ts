import {Component, inject} from '@angular/core';
import {
  FormArray,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {
  BoatId,
  CreatePaymentDTO,
  UserControllerService,
  UserDTO,
  UserId,
  WritePaymentControllerService
} from '../../../api';
import {ActivatedRoute} from '@angular/router';
import {MatIcon} from '@angular/material/icon';
import {ButtonDirective} from '../../../shared/button/button.directive';
import {ToastyService} from '../../../services/toasty/toasty.service';
import {catchError, throwError} from 'rxjs';
import {ErrorInfo} from '../../../model/ErrorInfo';

interface PaymentForm {
  amount: FormControl<number | null>
  reason: FormControl<string | null>
  isFuelPayment: FormControl<boolean>
  debts: FormArray<FormGroup<DebtForm>>
}

interface DebtForm {
  amount: FormControl<number | null>
  userId: FormControl<UserId | null>
}

@Component({
  selector: 'bs-payment-form',
  imports: [
    ReactiveFormsModule,
    MatIcon,
    ButtonDirective
  ],
  templateUrl: './payment-form.component.html',
  styleUrl: './payment-form.component.css'
})
export class PaymentFormComponent {
  protected form: FormGroup<PaymentForm>
  private fb = inject(NonNullableFormBuilder)
  private userService = inject(UserControllerService)
  private activatedRoute = inject(ActivatedRoute);
  private writePaymentService = inject(WritePaymentControllerService)
  private toastyService = inject(ToastyService)

  private boatId!: BoatId
  protected users!: UserDTO[]

  constructor() {
    this.form = this.fb.group<PaymentForm>({
      amount: this.fb.control(null, [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]),
      reason: this.fb.control('Benzin', Validators.required),
      isFuelPayment: this.fb.control(true, Validators.required),
      debts: this.fb.array<FormGroup<DebtForm>>([])
    })
    this.activatedRoute.data.subscribe(({boatId}) => {
      this.boatId = boatId
      this.userService.getAllUsersByBoatId(this.boatId.value!.toString())
        .subscribe(users => this.users = users)
    })
  }

  get debts(): FormArray<FormGroup<DebtForm>> {
    return this.form.get('debts') as FormArray<FormGroup<DebtForm>>;
  }

  addDebt(): void {
    const debtGroup = this.fb.group<DebtForm>({
      amount: this.fb.control(null, [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]),
      userId: this.fb.control(null, Validators.required)
    })
    this.form.controls.debts.push(debtGroup)
  }

  onSubmit() {
    if (this.form.invalid) {
      return
    }

    this.writePaymentService.create1(this.composePayload())
      .pipe(catchError((err: ErrorInfo) => {
        this.toastyService.addErrorNotification(`Fehler beim erstellen der Zahlung: ${err.msg}`)
        return throwError(() => err)
      }))
      .subscribe(res => {
        this.toastyService.addInfoNotification('Zahlung erstellt')
        this.clearForm()
      })
  }

  private composePayload(): CreatePaymentDTO {
    const values = this.form.getRawValue()

    return {
      amount: values.amount!,
      reason: values.reason!,
      isFuelPayment: values.isFuelPayment,
      debts: values.debts.map(debt => {
        return {amount: debt.amount!, userId: debt.userId!}
      })
    }
  }

  onFuelPaymentChange() {
    if (this.form.controls.isFuelPayment.getRawValue().valueOf()) {
      this.form.controls.debts.clear()
      this.form.controls.reason.setValue('Benzin')
    } else {
      this.form.controls.reason.setValue(null)
    }
  }

  private clearForm(): void {
    this.form.reset()
    this.form.controls.debts.clear()
  }
}
