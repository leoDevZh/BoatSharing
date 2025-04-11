import {Injectable, Signal, signal} from '@angular/core';

export interface Toasty {
  id: string
  message: string
  type: 'ERROR' | 'NOTIFICATION'
}

@Injectable({
  providedIn: 'root'
})
export class ToastyService {

  private toasties = signal<Toasty[]>([])

  getNotifications(): Signal<Toasty[]> {
    return this.toasties.asReadonly()
  }

  addErrorNotification(msg: string) {
    this.toasties.update(currentToasties => [...currentToasties, {
      id: this.generateUniqueId(),
      message: msg,
      type: 'ERROR'
    }])
  }

  addInfoNotification(msg: string) {
    this.toasties.update(currentToasties => [...currentToasties, {
      id: this.generateUniqueId(),
      message: msg,
      type: 'NOTIFICATION'
    }])
  }

  removeNotification(id: string) {
    this.toasties.update(currentToasties => currentToasties.filter(toasty => toasty.id !== id))
  }

  private generateUniqueId(): string {
    return `toasty-${Date.now()}-${Math.random().toString(36).substring(2, 15)}`;
  }
}
