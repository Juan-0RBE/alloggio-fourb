export type TipoPeriodo = 'BLOQUEADO' | 'RESERVADO';

/** Espejo de PeriodoDisponibilidadDTO del back. Las fechas van en formato ISO (yyyy-MM-dd). */
export interface PeriodoDisponibilidadDTO {
  idPeriodoDisponibilidad?: number;
  fechaInicio: string;
  fechaFin: string;
  tipo?: TipoPeriodo;
  idPublicacion: number;
  idReserva?: number | null;
}
