import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TabMenuComponent, TabMenuItem} from './tab-menu.component';
import {Component} from '@angular/core';

@Component({
  template: "<p>ONE</p>"
})
class TabOne {
}

@Component({
  template: "<p>TWO</p>"
})
class TabTwo {
}

describe('TabMenuComponent', () => {
  let component: TabMenuComponent;
  let fixture: ComponentFixture<TabMenuComponent>;
  const mockTabs: TabMenuItem[] = [{title: 'one', component: TabOne}, {title: 'two', component: TabTwo}]

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TabMenuComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(TabMenuComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('tabs', mockTabs)
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render first tab by default', () => {
    const liElements = fixture.nativeElement.querySelectorAll('li')
    const renderedContent = fixture.nativeElement.querySelector('p')
    expect(component.activeTab).toBe(0)
    expect(liElements[0].classList.contains('active')).toBeTrue()
    expect(liElements[1].classList.contains('active')).toBeFalse()
    expect(component.activeComponent).toBe(mockTabs[0].component)
    expect(renderedContent.textContent).toBe('ONE')
  })

  it('should change active tab on click', () => {
    const liElements = fixture.nativeElement.querySelectorAll('li')
    liElements[1].click()
    fixture.detectChanges()
    const renderedContent = fixture.nativeElement.querySelector('p')
    expect(component.activeTab).toBe(1)
    expect(liElements[0].classList.contains('active')).toBeFalse()
    expect(liElements[1].classList.contains('active')).toBeTrue()
    expect(component.activeComponent).toBe(mockTabs[1].component)
    expect(renderedContent.textContent).toBe('TWO')
  })
});
