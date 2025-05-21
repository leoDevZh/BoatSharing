import {ComponentFixture, TestBed} from '@angular/core/testing';

import {HomeComponent} from './home.component';
import {Component, input} from '@angular/core';
import {TabMenuItem} from '../shared/tab-menu/tab-menu.component';

@Component({
  selector: 'bs-tab-menu',
  standalone: true,
  template: ''
})
class TabMenuStubComponent {
  tabs = input<TabMenuItem[]>()
}

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent]
    })
      .overrideComponent(HomeComponent, {
        set: {
          imports: [TabMenuStubComponent]
        }
      })
      .compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
