import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResultadoReservaComponent } from './resultado-reserva.component';

describe('ResultadoReservaComponent', () => {
  let component: ResultadoReservaComponent;
  let fixture: ComponentFixture<ResultadoReservaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResultadoReservaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ResultadoReservaComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
