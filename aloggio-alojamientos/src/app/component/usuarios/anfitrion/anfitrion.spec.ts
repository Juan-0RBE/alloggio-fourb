import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AnfitrionComponent } from './anfitrion';

describe('AnfitrionComponent', () => {
  let component: AnfitrionComponent;
  let fixture: ComponentFixture<AnfitrionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnfitrionComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AnfitrionComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
