import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CalendarioDisponibilidadComponent } from './calendario-disponibilidad';

describe('CalendarioDisponibilidadComponent', () => {
  let component: CalendarioDisponibilidadComponent;
  let fixture: ComponentFixture<CalendarioDisponibilidadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarioDisponibilidadComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CalendarioDisponibilidadComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
