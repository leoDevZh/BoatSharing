import {Directive, HostBinding, input} from '@angular/core';

type ButtonType = 'cancel' | 'submit';
type ButtonSize = 'sm' | 'md' | 'lg'

@Directive({
  selector: 'button[bsButton]'
})
export class ButtonDirective {
  type = input.required<ButtonType>({alias: 'bsButton'})
  size = input.required<ButtonSize>()
  margin = input<string>('0')
  disabled = input<boolean>(false)

  @HostBinding('class') get buttonClass(): string {
    let classes = `btn-${this.type()} bs-btn`
    if (this.disabled()) {
      classes += ' disabled'
    }
    return classes
  }

  @HostBinding('style.margin') get buttonMargin(): string {
    return this.margin()
  }

  @HostBinding('style.width') get buttonWidth(): string {
    switch (this.size()) {
      case "sm":
        return '80px'
      case "md":
        return '110px'
      case "lg":
        return '160px'
    }
  }

  @HostBinding('style.height') get buttonHeight(): string {
    switch (this.size()) {
      case "sm":
        return '40px'
      case "md":
        return '50px'
      case "lg":
        return '60px'
    }
  }

  @HostBinding('style.padding') get buttonPadding(): string {
    switch (this.size()) {
      case 'sm':
        return 'var(--padding-xs)';
      case 'md':
        return 'var(--padding-m)';
      case 'lg':
        return 'var(--padding-l)';
      default:
        return 'var(--padding-m)';
    }
  }


  @HostBinding('style.fontSize') get buttonFontSize(): string {
    switch (this.size()) {
      case 'sm':
        return 'var(--font-size-xs)';
      case 'md':
        return 'var(--font-size-m)';
      case 'lg':
        return 'var(--font-size-l)';
      default:
        return 'var(--font-size-m)';
    }
  }

  @HostBinding('attr.disabled') get disabledAttr(): boolean | null {
    return this.disabled() ? this.disabled() : null;
  }

}
