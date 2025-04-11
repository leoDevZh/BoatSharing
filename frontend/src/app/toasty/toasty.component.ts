import {AfterViewInit, Component, ElementRef, inject, OnInit, Signal, ViewChild} from '@angular/core';
import {Toasty, ToastyService} from '../services/toasty/toasty.service';
import {NgClass} from '@angular/common';

@Component({
  selector: 'bs-toasty',
  imports: [
    NgClass
  ],
  templateUrl: './toasty.component.html',
  styleUrl: './toasty.component.css',
})
export class ToastyComponent implements OnInit, AfterViewInit {
  private toastyService = inject<ToastyService>(ToastyService)
  toasties!: Signal<Toasty[]>
  exitingToasties = new Set<string>()
  @ViewChild('toasty') toastyRef?: ElementRef

  ngOnInit(): void {
    this.toasties = this.toastyService.getNotifications()
  }

  ngAfterViewInit() {
    const height = this.toastyRef?.nativeElement.clientHeight
    document.documentElement.style.setProperty('--toasty-height', `${height}px`)
  }

  getClassForType(toasty: Toasty): string {
    switch (toasty.type) {
      case 'NOTIFICATION':
        return 'toasty-notification';
      case 'ERROR':
        return 'toasty-error';
      default:
        return 'toasty-notification';
    }
  }

  getTitle(toasty: Toasty): string {
    switch (toasty.type) {
      case 'NOTIFICATION':
        return 'Info'
      case 'ERROR':
        return 'Warning'
    }
  }

  removeToasty(id: string) {
    this.exitingToasties.add(id)
    setTimeout(() => {
      this.toastyService.removeNotification(id)
      this.exitingToasties.delete(id)
    }, 300)
  }
}
