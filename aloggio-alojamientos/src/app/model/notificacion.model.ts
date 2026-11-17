export type TipoNotificacion = 'RESERVA' | 'PAGO' | 'SISTEMA' | 'ALERTA';

/** Espejo de NotificacionDTO del back. */
export interface NotificacionDTO {
  idNotificacion: number;
  tipoNotificacion: TipoNotificacion;
  mensaje: string;
  /** Fecha-hora local del servidor (yyyy-MM-ddTHH:mm:ss). */
  fecha: string;
  estaLeida: boolean;
  idUsuario?: number;
}
