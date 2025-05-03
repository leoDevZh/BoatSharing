import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {HeaderComponent} from './header/header.component';
import {ToastyComponent} from './toasty/toasty.component';

@Component({
  selector: 'bs-root',
  imports: [RouterOutlet, HeaderComponent, ToastyComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
}
