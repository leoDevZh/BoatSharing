import {ComponentFixture, TestBed} from '@angular/core/testing';

import {MonthSelectorComponent} from './month-selector.component';
import {DateTime} from 'luxon';

describe('MonthSelectorComponent', () => {
  let component: MonthSelectorComponent;
  let fixture: ComponentFixture<MonthSelectorComponent>;
  const now = DateTime.now().startOf('month')

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MonthSelectorComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(MonthSelectorComponent);
    fixture.componentRef.setInput('month', now)
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component based on current month', () => {
    const displayed = fixture.nativeElement.querySelector('p')

    expect(displayed.innerText).toBe(`${now.monthShort}, ${now.year}`)
  })

  it('should go to next month on click chevron right', () => {
    const expected = DateTime.now().startOf('month').plus({month: 1})
    const iconRight = fixture.nativeElement.querySelectorAll('.icon')[1]

    iconRight.click()
    fixture.detectChanges()

    const actual = component.month()
    const displayed = fixture.nativeElement.querySelector('p')
    expect(actual.year).toBe(expected.year)
    expect(actual.month).toBe(expected.month)
    expect(actual.day).toBe(expected.day)
    expect(displayed.innerText).toBe(`${expected.monthShort}, ${expected.year}`)
  })

  it('should go to previous month on click chevron left', () => {
    const expected = DateTime.now().startOf('month').minus({month: 1})
    const iconLeft = fixture.nativeElement.querySelectorAll('.icon')[0]

    iconLeft.click()
    fixture.detectChanges()

    const actual = component.month()
    const displayed = fixture.nativeElement.querySelector('p')
    expect(actual.year).toBe(expected.year)
    expect(actual.month).toBe(expected.month)
    expect(actual.day).toBe(expected.day)
    expect(displayed.innerText).toBe(`${expected.monthShort}, ${expected.year}`)
  })
});
