export type EstadoPublicacion = 'BORRADOR' | 'ACTIVA' | 'PAUSADA' | 'BLOQUEADA';
export type TipoAlojamiento = 'CASA' | 'APARTAMENTO' | 'HABITACION' | 'CABAÑA';

// Espejo de los DTO del back (co.edu.unbosque.alloggio.dto)

export interface PublicacionDTO {
  idPublicacion?: number;
  titulo: string;
  descripcion: string;
  ubicacion: string;
  ciudad: string;
  tipo: TipoAlojamiento;
  capacidad: number;
  precioNoche: number;
  tarifaLimpieza: number;
  estado?: EstadoPublicacion;
  motivoBloqueo?: string | null;
  idAnfitrion?: number;
  idPoliticaCancelacion: number | null;
}

export interface ImagenAlojamientoDTO {
  idImagen?: number;
  urlImagen: string;
  orden?: number;
  idPublicacion?: number;
}

export interface ServicioAlojamientoDTO {
  idServicio?: number;
  nombre: string;
  costo: number;
  esAdicional?: boolean;
  idPublicacion?: number;
}

export interface ReglaAlojamientoDTO {
  idRegla?: number;
  descripcion: string;
  idPublicacion?: number;
}

export interface PoliticaCancelacionDTO {
  idPolitica: number;
  nombre: string;
  porcentajeDevolucion: number;
  plazoCancelacion: number;
  condiciones: string;
}
