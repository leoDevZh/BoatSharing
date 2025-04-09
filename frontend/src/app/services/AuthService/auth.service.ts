import {inject, Injectable, InjectionToken} from '@angular/core';
import {LocalStorageService} from '../localstorage/local-storage.service';

export const TOKEN_KEY = new InjectionToken<string>('TOKEN_KEY', {
  providedIn: 'root',
  factory: () => 'token'
})

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private storageService = inject(LocalStorageService)
  private tokenKey = inject(TOKEN_KEY)

  getToken(): string | null {
    return this.storageService.get(this.tokenKey)
  }

  setToken(token: string): void {
    this.storageService.set(this.tokenKey, token)
  }
}
