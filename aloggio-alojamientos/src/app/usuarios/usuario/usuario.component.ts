import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

const REGEX_NOMBRE = /^\p{L}[\p{L} '\-]{0,49}$/u;
const REGEX_TELEFONO = /^\+?\d{7,15}$/;
const REGEX_CARACTER_ESPECIAL = /[!@#$%^&*(),.?":{}|<>]/;

type SeccionPerfil = 'informacion' | 'seguridad' | 'cuenta';
type CampoNombre = 'primerNombre' | 'segundoNombre' | 'primerApellido' | 'segundoApellido';
type CampoClave = 'claveActual' | 'claveNueva' | 'confirmarClave';
type CampoSoloLectura =
  'correoUsuario' | 'nombreUsuario' | 'documentoIdentidad' | 'fechaNacimiento';

interface PerfilUsuario {
  correoUsuario: string;
  nombreUsuario: string;
  documentoIdentidad: string;
  fechaNacimiento: string;
  isVerified: boolean;
  role: string;
}

type FormularioPerfil = Record<CampoNombre, string> & { numeroTelefono: string };

interface ConfigCampoNombre {
  campo: CampoNombre;
  etiqueta: string;
  placeholder: string;
  obligatorio: boolean;
}

@Component({
  imports: [CommonModule, FormsModule, RouterModule],
  selector: 'app-usuario.component',
  styleUrl: './usuario.component.scss',
  templateUrl: './usuario.component.html',
})
export class Usuario {
  seccionActiva: SeccionPerfil = 'informacion';

  readonly opcionesMenu: { id: SeccionPerfil; etiqueta: string; icono: string }[] = [
    { id: 'informacion', etiqueta: 'Información personal', icono: 'pi-user' },
    { id: 'seguridad', etiqueta: 'Seguridad', icono: 'pi-lock' },
    { id: 'cuenta', etiqueta: 'Cuenta', icono: 'pi-cog' },
  ];

  // Datos de solo lectura: el backend solo permite editar nombres y teléfono
  perfil: PerfilUsuario = {
    correoUsuario: '',
    nombreUsuario: '',
    documentoIdentidad: '',
    fechaNacimiento: '',
    isVerified: false,
    role: 'USUARIO',
  };

  readonly camposSoloLectura: { campo: CampoSoloLectura; etiqueta: string; icono: string }[] = [
    { campo: 'correoUsuario', etiqueta: 'Correo electrónico', icono: 'pi-envelope' },
    { campo: 'nombreUsuario', etiqueta: 'Nombre de usuario', icono: 'pi-at' },
    { campo: 'documentoIdentidad', etiqueta: 'Documento de identidad', icono: 'pi-id-card' },
    { campo: 'fechaNacimiento', etiqueta: 'Fecha de nacimiento', icono: 'pi-calendar' },
  ];

  // Información personal
  readonly camposNombre: ConfigCampoNombre[] = [
    {
      campo: 'primerNombre',
      etiqueta: 'Primer nombre',
      placeholder: 'Ej. María',
      obligatorio: true,
    },
    {
      campo: 'segundoNombre',
      etiqueta: 'Segundo nombre',
      placeholder: 'Opcional',
      obligatorio: false,
    },
    {
      campo: 'primerApellido',
      etiqueta: 'Primer apellido',
      placeholder: 'Ej. Gómez',
      obligatorio: true,
    },
    {
      campo: 'segundoApellido',
      etiqueta: 'Segundo apellido',
      placeholder: 'Opcional',
      obligatorio: false,
    },
  ];

  datosGuardados: FormularioPerfil = this.crearFormularioVacio();
  formulario: FormularioPerfil = { ...this.datosGuardados };
  intentoGuardar: boolean = false;

  // Seguridad
  readonly camposClave: { campo: CampoClave; etiqueta: string }[] = [
    { campo: 'claveActual', etiqueta: 'Contraseña actual' },
    { campo: 'claveNueva', etiqueta: 'Nueva contraseña' },
    { campo: 'confirmarClave', etiqueta: 'Confirmar nueva contraseña' },
  ];

  cambioClave: Record<CampoClave, string> = {
    claveActual: '',
    claveNueva: '',
    confirmarClave: '',
  };

  mostrarClave: Record<CampoClave, boolean> = {
    claveActual: false,
    claveNueva: false,
    confirmarClave: false,
  };

  readonly requisitosClave: { texto: string; cumple: (clave: string) => boolean }[] = [
    { texto: 'Mínimo 8 caracteres', cumple: (clave) => clave.length >= 8 },
    { texto: 'Una letra mayúscula', cumple: (clave) => /[A-Z]/.test(clave) },
    { texto: 'Una letra minúscula', cumple: (clave) => /[a-z]/.test(clave) },
    { texto: 'Un número', cumple: (clave) => /\d/.test(clave) },
    {
      texto: 'Un carácter especial (!@#$%…)',
      cumple: (clave) => REGEX_CARACTER_ESPECIAL.test(clave),
    },
  ];

  // Cuenta
  mostrarConfirmacion: boolean = false;
  confirmarDesactivacion: boolean = false;

  get nombreCompleto(): string {
    const g = this.datosGuardados;
    return [g.primerNombre, g.segundoNombre, g.primerApellido, g.segundoApellido]
      .filter((parte) => parte)
      .join(' ');
  }

  get iniciales(): string {
    const g = this.datosGuardados;
    const letras = (g.primerNombre.charAt(0) + g.primerApellido.charAt(0)).toUpperCase();
    return letras || '?';
  }

  get hayCambios(): boolean {
    const f = this.formulario;
    const g = this.datosGuardados;
    return (
      this.camposNombre.some((c) => f[c.campo].trim() !== g[c.campo]) ||
      f.numeroTelefono.trim() !== g.numeroTelefono
    );
  }

  get informacionValida(): boolean {
    return (
      this.camposNombre.every((c) =>
        this.esNombreValido(this.formulario[c.campo], c.obligatorio),
      ) && REGEX_TELEFONO.test(this.formulario.numeroTelefono.trim())
    );
  }

  get clavesCoinciden(): boolean {
    return this.cambioClave.claveNueva === this.cambioClave.confirmarClave;
  }

  get claveValida(): boolean {
    return (
      this.cambioClave.claveActual.length > 0 &&
      this.requisitosClave.every((r) => r.cumple(this.cambioClave.claveNueva)) &&
      this.clavesCoinciden
    );
  }

  valorSoloLectura(campo: CampoSoloLectura): string {
    const valor = this.perfil[campo];
    if (campo === 'fechaNacimiento' && valor) {
      // Se fuerza la hora local para que la fecha no se corra por la zona horaria
      return new Date(valor + 'T00:00:00').toLocaleDateString('es-CO');
    }
    return valor;
  }

  errorNombre(config: ConfigCampoNombre): string {
    const valor = this.formulario[config.campo].trim();
    if (!valor) {
      return config.obligatorio && this.intentoGuardar ? 'Este campo es obligatorio.' : '';
    }
    return this.esNombreValido(valor, config.obligatorio)
      ? ''
      : 'Solo letras, espacios, apóstrofos y guiones (máximo 50).';
  }

  errorTelefono(): string {
    const valor = this.formulario.numeroTelefono.trim();
    if (!valor) {
      return this.intentoGuardar ? 'El número de teléfono es obligatorio.' : '';
    }
    return REGEX_TELEFONO.test(valor) ? '' : 'Ingresa de 7 a 15 dígitos (puede iniciar con +).';
  }

  restablecerFormulario() {
    this.formulario = { ...this.datosGuardados };
    this.intentoGuardar = false;
  }

  alGuardarPerfil() {
    this.intentoGuardar = true;
    if (!this.informacionValida) {
      return;
    }
    console.log('Guardando perfil...');
  }

  alCambiarClave() {
    if (!this.claveValida) {
      return;
    }
    console.log('Actualizando contraseña...');
  }

  cancelarDesactivacion() {
    this.mostrarConfirmacion = false;
    this.confirmarDesactivacion = false;
  }

  alDesactivarCuenta() {
    if (!this.confirmarDesactivacion) {
      return;
    }
    console.log('Desactivando cuenta...');
  }

  private esNombreValido(valor: string, obligatorio: boolean): boolean {
    const texto = valor.trim();
    return texto ? REGEX_NOMBRE.test(texto) : !obligatorio;
  }

  private crearFormularioVacio(): FormularioPerfil {
    return {
      primerNombre: '',
      segundoNombre: '',
      primerApellido: '',
      segundoApellido: '',
      numeroTelefono: '',
    };
  }
}
