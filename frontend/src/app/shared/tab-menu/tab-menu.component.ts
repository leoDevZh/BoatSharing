import {Component, HostBinding, input} from '@angular/core';
import {NgComponentOutlet} from '@angular/common';

export interface TabMenuItem {
  title: string
  component: any
}

@Component({
  selector: 'bs-tab-menu',
  imports: [
    NgComponentOutlet
  ],
  templateUrl: './tab-menu.component.html',
  styleUrl: './tab-menu.component.css'
})
export class TabMenuComponent {
  tabs = input.required<TabMenuItem[]>()
  activeTab = 0

  @HostBinding('style.--active-tab')
  get leftOffset(): number {
    return this.activeTab
  }

  activateTab(index: number): void {
    this.activeTab = index
  }

  get activeComponent(): any {
    return this.tabs()[this.activeTab].component
  }
}
