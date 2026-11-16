export interface UsuarioDTO {
  nombreUsuario: string;
  claveUsuario: string;
  correoUsuario: string;

}

export interface AuthResponse {
  token: string;
  role: string;
}

export interface PerfilDTO {
  correoUsuario?: string;
  nombreUsuario?: string;
  documentoIdentidad?: string;
  fechaNacimiento?: string;
  isVerified?: boolean;
  role?: string;
  primerNombre?: string;
  segundoNombre?: string | null;
  primerApellido?: string;
  segundoApellido?: string | null;
  numeroTelefono?: string;
}

export interface ActualizarPerfilDTO {
  primerNombre: string;
  segundoNombre: string | null;
  primerApellido: string;
  segundoApellido: string | null;
  numeroTelefono: string;
}
