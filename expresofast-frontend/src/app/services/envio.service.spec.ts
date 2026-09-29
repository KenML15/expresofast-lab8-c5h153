import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EnvioService } from './envio.service';
import { environment } from '../../environments/environment';

describe('EnvioService', () => {
  let service: EnvioService;
  let http: HttpTestingController;
  const base = `${environment.API_URL}envios`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EnvioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('obtenerEnvios hace GET a /envios', () => {
    service.obtenerEnvios().subscribe();
    expect(http.expectOne(base).request.method).toBe('GET');
  });

  it('obtenerPorRastreo hace GET a /envios/rastreo/{codigo}', () => {
    service.obtenerPorRastreo(' EXP-2026-1001 ').subscribe();
    expect(http.expectOne(`${base}/rastreo/EXP-2026-1001`).request.method).toBe('GET');
  });

  it('crearEnvio hace POST con el payload', () => {
    const payload = { destinatario: 'Ana', direccionDestino: 'Limón', montoFlete: 5000 };
    service.crearEnvio(payload).subscribe();
    const req = http.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
  });

  it('actualizarEstado hace PATCH a /envios/{id}/estado', () => {
    service.actualizarEstado(7, 'EN_TRANSITO').subscribe();
    const req = http.expectOne(`${base}/7/estado`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body.nuevoEstado).toBe('EN_TRANSITO');
  });
});
