import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormularioCancelacionComponent } from './formulario-cancelacion.component';

describe('FormularioCancelacionComponent', () => {
  let component: FormularioCancelacionComponent;
  let fixture: ComponentFixture<FormularioCancelacionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioCancelacionComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioCancelacionComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
