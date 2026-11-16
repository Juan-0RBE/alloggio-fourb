import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BandejaNotificacionesComponent } from './bandeja-notificaciones';

describe('BandejaNotificacionesComponent', () => {
  let component: BandejaNotificacionesComponent;
  let fixture: ComponentFixture<BandejaNotificacionesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BandejaNotificacionesComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BandejaNotificacionesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
