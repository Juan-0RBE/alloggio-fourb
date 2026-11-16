import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactivarCuentaComponent } from './reactivar-cuenta';

describe('ReactivarCuentaComponent', () => {
  let component: ReactivarCuentaComponent;
  let fixture: ComponentFixture<ReactivarCuentaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactivarCuentaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ReactivarCuentaComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
