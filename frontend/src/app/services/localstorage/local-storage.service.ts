import {inject, Injectable, InjectionToken} from '@angular/core';

// Inject localStorage using InjectionToken to improve testability
export const BROWSER_STORAGE = new InjectionToken<Storage>('Browser Storage', {
  providedIn: 'root',
  factory: () => localStorage
})

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {

  private storage = inject(BROWSER_STORAGE)

  get(key: string): string | null {
    return this.storage.getItem(key)
  }

  set(key: string, value: string): void {
    this.storage.setItem(key, value)
  }
}
