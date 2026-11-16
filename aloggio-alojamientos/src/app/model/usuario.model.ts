export interface UsuarioDTO {
  nombreUsuario: string;
  claveUsuario: string;
  correoUsuario: string;

}

export interface AuthResponse {
  token: string;
  role: string;
}
