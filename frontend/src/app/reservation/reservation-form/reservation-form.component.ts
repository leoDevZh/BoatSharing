import {Component, inject, input, model, OnChanges, OnInit, output, SimpleChanges} from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {provideNativeDateAdapter} from '@angular/material/core';
import {DateTime} from 'luxon';
import {BoatId, CreateReservationDTO, ReservationUserDTO, UpdateReservationDTO} from '../../api';
import {MatError, MatFormField} from '@angular/material/form-field';
import {MatInput, MatLabel} from '@angular/material/input';
import {ButtonDirective} from '../../shared/button/button.directive';
import {ReservationService} from '../../services/reservation/reservation.service';
import {ActivatedRoute} from '@angular/router';
import {ToastyService} from '../../services/toasty/toasty.service';

interface ReservationDialogForm {
  startDateTime: FormControl<string | null>
  endDateTime: FormControl<string | null>
  startHours: FormControl<number | null>
  endHours: FormControl<number | null>
}

@Component({
  selector: 'bs-reservation-form',
  providers: [provideNativeDateAdapter()],
  imports: [
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatLabel,
    ButtonDirective,
    MatError
  ],
  templateUrl: './reservation-form.component.html',
  styleUrl: './reservation-form.component.css'
})
export class ReservationFormComponent implements OnInit, OnChanges {
  selectedDay = input.required<DateTime>()
  reservationToUpdate = model<ReservationUserDTO | undefined>(undefined)
  reloadReservations = output<void>()

  form!: FormGroup<ReservationDialogForm>
  private boatId!: BoatId

  private fb = inject(NonNullableFormBuilder)
  private reservationService = inject(ReservationService)
  private activatedRoute = inject(ActivatedRoute);
  private toastyService = inject(ToastyService)

  ngOnInit(): void {
    const toUpdate = this.reservationToUpdate()
    this.form = this.fb.group<ReservationDialogForm>({
      startDateTime: this.fb.control(toUpdate ? DateTime.fromISO(toUpdate.startDateTime!).toFormat("HH:mm") : null, Validators.required),
      endDateTime: this.fb.control(toUpdate ? DateTime.fromISO(toUpdate.endDateTime!).toFormat("HH:mm") : null, Validators.required),
      startHours: this.fb.control(toUpdate?.boatHoursOnStart ? toUpdate.boatHoursOnStart : null, [Validators.min(0), this.createNumbersOnlyValidator()]),
      endHours: this.fb.control(toUpdate?.boatHoursOnEnd ? toUpdate.boatHoursOnEnd : null, [Validators.min(0)])
    })

    this.activatedRoute.data.subscribe(({boatId}) => {
      this.boatId = boatId
    })
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['reservationToUpdate'] && !changes['reservationToUpdate'].isFirstChange()) {
      const toUpdate = changes['reservationToUpdate'].currentValue
      if (toUpdate === undefined) {
        this.clearForm()
      } else {
        this.initFormWithReservationToUpdate(toUpdate);
      }
    }
  }

  onSubmit() {
    if (!this.form.valid) {
      return
    }
    if (this.reservationToUpdate() === undefined) {
      const createReservationDTO = this.formToCreateReservationDTO()
      this.reservationService.createReservation(createReservationDTO)
        .subscribe(() => {
          this.toastyService.addInfoNotification("Reservation erstellt")
          this.reloadReservations.emit()
          this.clearForm()
        })
    } else {
      const updateReservationDTO = this.formToUpdateReservationDTO()
      this.reservationService.updateReservation(updateReservationDTO)
        .subscribe(() => {
          this.toastyService.addInfoNotification("Reservation updated")
          this.reloadReservations.emit()
          this.clearForm()
        })
    }
  }

  onDelete() {
    this.reservationService.deleteReservation(this.reservationToUpdate()?.reservationId!)
      .subscribe(() => {
        this.toastyService.addInfoNotification("Reservation gelöscht")
        this.reservationToUpdate.set(undefined)
        this.reloadReservations.emit()
        this.clearForm()
      })
  }

  onCancel() {
    this.clearForm();
  }

  private initFormWithReservationToUpdate(toUpdate: ReservationUserDTO) {
    this.form.reset({
      startDateTime: DateTime.fromISO(toUpdate.startDateTime!).toFormat("HH:mm"),
      endDateTime: DateTime.fromISO(toUpdate.endDateTime!).toFormat("HH:mm"),
      startHours: toUpdate?.boatHoursOnStart ? toUpdate.boatHoursOnStart : null,
      endHours: toUpdate?.boatHoursOnEnd ? toUpdate.boatHoursOnEnd : null
    })
  }

  private clearForm() {
    this.reservationToUpdate.set(undefined)
    this.form.reset({
      startDateTime: null,
      endDateTime: null,
      startHours: null,
      endHours: null
    })
  }

  private createNumbersOnlyValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value

      if (value === null || value === '') {
        return null
      }

      const numberValue = parseInt(value)
      if (Number.isSafeInteger(numberValue)) {
        return null
      }

      return {notANumber: 'Input ist keine Zahl'}
    }
  }

  private formToCreateReservationDTO(): CreateReservationDTO {
    const rawValues = this.form.getRawValue()

    return {
      boatId: this.reservationToUpdate() ? this.reservationToUpdate()?.boatId! : this.boatId,
      startDateTime: this.applyTimeToDate(rawValues.startDateTime!),
      endDateTime: this.applyTimeToDate(rawValues.endDateTime!)
    }
  }

  private formToUpdateReservationDTO(): UpdateReservationDTO {
    const rawValues = this.form.getRawValue()
    return {
      reservationId: this.reservationToUpdate()?.reservationId!,
      startDateTime: this.applyTimeToDate(rawValues.startDateTime!),
      endDateTime: this.applyTimeToDate(rawValues.endDateTime!),
      boatEngineHoursOnStart: rawValues.startHours ?? undefined,
      boatEngineHoursOnEnd: rawValues.endHours ?? undefined
    }
  }

  private applyTimeToDate(timeStr: string): string {
    let dateString = this.selectedDay().toFormat('yyyy-MM-dd')
    dateString += `T${timeStr}`
    return dateString
  }

  submitButtonDisabled() {
    return !this.form.valid;
  }
}
