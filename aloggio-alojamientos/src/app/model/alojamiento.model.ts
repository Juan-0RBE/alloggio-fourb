import {
  ImagenAlojamientoDTO,
  PoliticaCancelacionDTO,
  ReglaAlojamientoDTO,
  ServicioAlojamientoDTO,
  TipoAlojamiento,
} from './publicacion.model';

// Espejo de los DTO públicos de /alojamiento (co.edu.unbosque.alloggio.dto)

export interface AlojamientoResumenDTO {
  idPublicacion: number;
  titulo: string;
  ciudad: string;
  tipo: TipoAlojamiento;
  capacidad: number;
  precioNoche: number;
  imagenPrincipal: ImagenAlojamientoDTO | null;
}

export interface PaginaDTO<T> {
  contenido: T[];
  paginaActual: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

/** Rango [fechaInicio, fechaFin) no disponible. Fechas ISO yyyy-MM-dd. */
export interface RangoOcupadoDTO {
  fechaInicio: string;
  fechaFin: string;
}

export interface ResenaDTO {
  idResena: number;
  calificacion: number;
  comentario: string;
  fecha: string;
  autor: string;
  visible?: boolean;
  idReserva?: number;
}

export interface AlojamientoDetalleDTO {
  idPublicacion: number;
  titulo: string;
  descripcion: string;
  ubicacion: string;
  ciudad: string;
  tipo: TipoAlojamiento;
  capacidad: number;
  precioNoche: number;
  tarifaLimpieza: number | null;
  politicaCancelacion: PoliticaCancelacionDTO | null;
  servicios: ServicioAlojamientoDTO[];
  reglas: ReglaAlojamientoDTO[];
  imagenes: ImagenAlojamientoDTO[];
  calendarioOcupado: RangoOcupadoDTO[];
  promedioCalificacion: number | null;
  totalResenas: number;
  resenas: ResenaDTO[];
}

/** Criterios opcionales de /alojamiento/buscar. */
export interface CriteriosBusqueda {
  ciudad?: string;
  fechaLlegada?: string;
  fechaSalida?: string;
  precioMin?: number | null;
  precioMax?: number | null;
  capacidad?: number | null;
  tipo?: TipoAlojamiento | '';
  servicios?: string[];
  pagina?: number;
  tamano?: number;
}
