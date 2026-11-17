// Espejo de los DTO de /cotizacion y /reserva (co.edu.unbosque.alloggio.dto)

export interface CotizacionServicioDTO {
  idServicio: number;
  nombre: string;
  costoAplicado: number;
}

export interface CotizacionDTO {
  idCotizacion?: number;
  idPublicacion: number;
  fechaLlegada: string;
  fechaSalida: string;
  noches?: number;
  precioBase?: number;
  limpieza?: number;
  servicio?: number;
  descuento?: number;
  serviciosAdicionales?: number;
  total?: number;
  /** Fecha-hora local del servidor (yyyy-MM-ddTHH:mm:ss) hasta la que se puede pagar. */
  vigencia?: string;
  servicios?: CotizacionServicioDTO[];
}

export interface ReservaDTO {
  idCotizacion?: number;
  metodoPago?: string;
  claveIdempotencia?: string;
  idReserva?: number;
  idPublicacion?: number;
  fechaLlegada?: string;
  fechaSalida?: string;
  total?: number;
  estado?: string;
  fechaCreacion?: string;
  motivoRechazo?: string;
  tituloPublicacion?: string;
  ciudadPublicacion?: string;
  urlImagenPrincipal?: string | null;
  nombrePolitica?: string | null;
  porcentajeDevolucion?: number | null;
  plazoCancelacion?: number | null;
  condicionesPolitica?: string | null;
  tieneResena?: boolean;
}

/** Espejo de CancelacionDTO: se envía el motivo y el back responde con la devolución. */
export interface CancelacionDTO {
  motivo: string;
  idCancelacion?: number;
  idReserva?: number;
  fecha?: string;
  valorDevolucion?: number;
  referenciaReembolso?: string | null;
  estadoReserva?: string;
}

export type EstadoReserva = 'CONFIRMADA' | 'COMPLETADA' | 'CANCELADA';
