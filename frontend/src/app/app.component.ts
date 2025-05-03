import {Component, inject, OnInit} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {HeaderComponent} from './header/header.component';
import {ToastyComponent} from './toasty/toasty.component';
import {AuthenticationControllerService} from './api';
import {AuthService} from './services/AuthService/auth.service';

@Component({
  selector: 'bs-root',
  imports: [RouterOutlet, HeaderComponent, ToastyComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private authController = inject(AuthenticationControllerService)
  private authService = inject(AuthService)

  ngOnInit(): void {
    this.authController.refresh().subscribe(response => {
      this.authService.setToken(response.token)
    })
  }
}
