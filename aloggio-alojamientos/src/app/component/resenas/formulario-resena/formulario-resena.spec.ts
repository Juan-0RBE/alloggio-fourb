import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormularioResena } from './formulario-resena';

describe('FormularioResena', () => {
  let component: FormularioResena;
  let fixture: ComponentFixture<FormularioResena>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioResena],
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioResena);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
