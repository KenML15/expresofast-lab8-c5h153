import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('muestra la barra de navegación con las 3 vistas cuando hay sesión', async () => {
    sessionStorage.setItem('jwt_token', 'token');
    sessionStorage.setItem('username', 'admin');
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const enlaces = Array.from<HTMLAnchorElement>(fixture.nativeElement.querySelectorAll('.nav-links a'));
    expect(enlaces.map((a) => a.getAttribute('href'))).toEqual(['/envios', '/nuevo-envio', '/rastreo']);
  });
});
