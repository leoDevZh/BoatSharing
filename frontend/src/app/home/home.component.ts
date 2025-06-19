import {Component} from '@angular/core';
import {TabMenuComponent, TabMenuItem} from '../shared/tab-menu/tab-menu.component';
import {ReservationComponent} from '../reservation/reservation.component';
import {TodoComponent} from '../todo/todo.component';
import {PaymentComponent} from '../payment/payment.component';

@Component({
  selector: 'bs-home',
  imports: [
    TabMenuComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  tabs: TabMenuItem[] = [
    {title: 'Ausfahrt', component: ReservationComponent},
    {title: 'ToDo', component: TodoComponent},
    {title: 'Zahlung', component: PaymentComponent}
  ];
}
