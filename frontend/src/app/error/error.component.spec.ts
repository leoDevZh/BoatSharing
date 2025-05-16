import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ErrorComponent} from './error.component';

describe('ErrorComponent', () => {
  let component: ErrorComponent;
  let fixture: ComponentFixture<ErrorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display content', () => {
    const title = fixture.nativeElement.querySelector('h1')
    const img = fixture.nativeElement.querySelector('img')
    expect(title).toBeTruthy()
    expect(img.src).toContain('sinking-boat.png')
  })
});
