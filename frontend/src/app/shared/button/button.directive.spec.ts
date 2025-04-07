import {ButtonDirective} from './button.directive';
import {Component} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';

@Component({
  imports: [
    ButtonDirective
  ],
  template: "<button [bsButton]=\"'submit'\" size='md' margin='8px' [disabled]='true'></button>"
})
class TestHostComponent {
}

describe('ButtonDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [ButtonDirective]
    })

    fixture = TestBed.createComponent(TestHostComponent)
    fixture.detectChanges()
  })

  it('should apply the correct class and styles', () => {
    const buttonEl = fixture.debugElement.query(By.css('button'));
    const nativeEl: HTMLButtonElement = buttonEl.nativeElement;

    expect(nativeEl.classList).toContain('btn-submit');
    expect(nativeEl.classList).toContain('bs-btn');
    expect(nativeEl.classList).toContain('disabled');
    expect(nativeEl.style.margin).toBe('8px');
    expect(nativeEl.style.width).toBe('110px');
    expect(nativeEl.style.height).toBe('50px');
    expect(nativeEl.style.padding).toBe('var(--padding-m)');
    expect(nativeEl.style.fontSize).toBe('var(--font-size-m)');
    expect(nativeEl.getAttribute('disabled')).toBe('true');
  });
});
