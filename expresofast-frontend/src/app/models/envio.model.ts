export type EstadoEnvio = 'PENDIENTE' | 'EN_TRANSITO' | 'ENTREGADO' | 'CANCELADO';

/** Envío tal como lo retorna la API (EnvioDTO del backend). */
export interface Envio {
  id: number;
  codigoRastreo: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  estado: EstadoEnvio;
  fechaCreacion: string | null;
}

/** Payload para registrar una guía nueva (CrearEnvioDTO del backend). */
export interface CrearEnvioPayload {
  destinatario: string;
  direccionDestino: string;
  montoFlete: number | null;
}

export const ESTADOS_ENVIO: { valor: EstadoEnvio; etiqueta: string }[] = [
  { valor: 'PENDIENTE', etiqueta: 'Pendiente' },
  { valor: 'EN_TRANSITO', etiqueta: 'En tránsito' },
  { valor: 'ENTREGADO', etiqueta: 'Entregado' },
  { valor: 'CANCELADO', etiqueta: 'Cancelado' },
];

export const ESTADOS_FINALES: EstadoEnvio[] = ['ENTREGADO', 'CANCELADO'];

export function etiquetaEstado(estado: EstadoEnvio | string): string {
  return ESTADOS_ENVIO.find((e) => e.valor === estado)?.etiqueta ?? estado;
}

/** Un paquete dentro del envío (PaqueteDTO del backend). */
export interface PaquetePayload {
  descripcion: string;
  pesoKg: number;
}

/** Payload del POST /envios/avanzado (EnvioRegistroDTO del backend). */
export interface EnvioRegistroPayload {
  numeroTracking: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  fechaDespacho: string;          // 'YYYY-MM-DD' (formato del <input type="date">)
  fechaEntregaEstimada: string;   // 'YYYY-MM-DD'
  paquetes: PaquetePayload[];
}

/** Respuesta del POST /envios/avanzado (EnvioConPaquetesDTO del backend). */
export interface EnvioConPaquetes {
  id: number;
  codigoRastreo: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  estado: EstadoEnvio;
  fechaDespacho: string;
  fechaEntregaEstimada: string;
  pesoTotalKg: number;
  paquetes: PaquetePayload[];
}

/** Respuesta del GET /envios/check-tracking/{trackingNumber}. */
export interface TrackingCheck {
  numeroTracking: string;
  existe: boolean;
}