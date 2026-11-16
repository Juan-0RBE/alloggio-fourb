import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { UsuarioService } from '../../../service/usuario.service';
import { AuthService } from '../../../service/auth.service';

const REGEX_NOMBRE = /^\p{L}[\p{L} '\-]{0,49}$/u;
const REGEX_TELEFONO = /^\+?\d{10}$/;
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
  styleUrl: './usuario.scss',
  templateUrl: './usuario.html',
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
  guardandoClave: boolean = false;
  errorClave: string = '';
  exitoClave: string = '';

  desactivando: boolean = false;
  errorCuenta: string = '';

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
    return REGEX_TELEFONO.test(valor) ? '' : 'El teléfono debe tener 10 dígitos';
  }

  restablecerFormulario() {
    this.formulario = { ...this.datosGuardados };
    this.intentoGuardar = false;
  }

  alCambiarClave() {
    this.errorClave = '';
    this.exitoClave = '';
    if (!this.claveValida || this.guardandoClave) return;

    this.guardandoClave = true;
    this.usuarioServ
      .cambiarClave(this.cambioClave.claveActual, this.cambioClave.claveNueva)
      .subscribe({
        next: (mensaje) => {
          this.guardandoClave = false;
          this.exitoClave = mensaje;
          this.cambioClave = { claveActual: '', claveNueva: '', confirmarClave: '' };
          this.mostrarClave = { claveActual: false, claveNueva: false, confirmarClave: false };
          this.cdr.detectChanges();
        },
        error: (err: HttpErrorResponse) => {
          this.guardandoClave = false;

          // 401/403 aquí = contraseña actual incorrecta, NO sesión expirada
          if (err.status === 401 || err.status === 403) {
            this.errorClave =
              typeof err.error === 'string' && err.error
                ? err.error
                : 'La contraseña actual no es correcta.';
          } else {
            this.manejarError(err, 'errorClave');
          }
          this.cdr.detectChanges();
        },
      });
  }
  cancelarDesactivacion() {
    this.mostrarConfirmacion = false;
    this.confirmarDesactivacion = false;
  }

  alDesactivarCuenta() {
    if (!this.confirmarDesactivacion || this.desactivando) {
      return;
    }
    this.errorCuenta = '';
    this.desactivando = true;

    this.usuarioServ.desactivarCuenta().subscribe({
      next: () => {
        this.desactivando = false;
        this.authServ.logout();
      },
      error: (err: HttpErrorResponse) => {
        this.desactivando = false;
        this.manejarError(err, 'errorCuenta');
      },
    });
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

  cargandoPerfil: boolean = false;
  guardando: boolean = false;
  errorCarga: string = '';
  errorGuardar: string = '';
  mensajeExito: string = '';

  constructor(
    private usuarioServ: UsuarioService,
    private authServ: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.cargarPerfil();
  }

  cargarPerfil() {
    this.cargandoPerfil = true;
    this.errorCarga = '';

    this.usuarioServ.obtenerPerfil().subscribe({
      next: (p) => {
        this.perfil = {
          correoUsuario: p.correoUsuario ?? '',
          nombreUsuario: p.nombreUsuario ?? '',
          documentoIdentidad: p.documentoIdentidad ?? '',
          fechaNacimiento: p.fechaNacimiento ?? '',
          isVerified: !!p.isVerified,
          role: p.role ?? 'USUARIO',
        };
        this.datosGuardados = {
          primerNombre: p.primerNombre ?? '',
          segundoNombre: p.segundoNombre ?? '',
          primerApellido: p.primerApellido ?? '',
          segundoApellido: p.segundoApellido ?? '',
          numeroTelefono: p.numeroTelefono ?? '',
        };
        this.formulario = { ...this.datosGuardados };
        this.cargandoPerfil = false;
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.cargandoPerfil = false;
        this.manejarError(err, 'errorCarga');
      },
    });
  }

  alGuardarPerfil() {
    this.intentoGuardar = true;
    this.errorGuardar = '';
    this.mensajeExito = '';

    if (!this.informacionValida || this.guardando) {
      return;
    }

    const f = this.formulario;
    const limpio: FormularioPerfil = {
      primerNombre: f.primerNombre.trim(),
      segundoNombre: f.segundoNombre.trim(),
      primerApellido: f.primerApellido.trim(),
      segundoApellido: f.segundoApellido.trim(),
      numeroTelefono: f.numeroTelefono.trim(),
    };

    this.guardando = true;
    this.usuarioServ
      .actualizarPerfil({
        ...limpio,
        // Los opcionales vacíos se envían como null
        segundoNombre: limpio.segundoNombre || null,
        segundoApellido: limpio.segundoApellido || null,
      })
      .subscribe({
        next: (mensaje) => {
          this.guardando = false;
          this.datosGuardados = limpio;
          this.formulario = { ...limpio };
          this.intentoGuardar = false;
          this.mensajeExito = mensaje;
          this.cdr.detectChanges();
        },
        error: (err: HttpErrorResponse) => {
          this.guardando = false;
          this.manejarError(err, 'errorGuardar');
        },
      });
  }

  private manejarError(
    err: HttpErrorResponse,
    destino: 'errorCarga' | 'errorGuardar' | 'errorClave' | 'errorCuenta',
  ) {
    if (err.status === 401 || err.status === 403) {
      this.authServ.logout();
      return;
    }
    this[destino] =
      typeof err.error === 'string' && err.error
        ? err.error
        : 'No se pudo conectar con el servidor.';
    this.cdr.detectChanges();
  }

  soloLetras(event: Event, campo: string): void {
    const input = event.target as HTMLInputElement;
    const limpio = input.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s'-]/g, '');

    if (input.value !== limpio) {
      input.value = limpio
    }
    // @ts-ignore
    this.formulario[campo] = limpio;
  }

  soloNumeros(event: Event): void {
    const input = event.target as HTMLInputElement;
    const limpio = input.value.replace(/\D/g, ''); // \D = todo lo que no sea dígito

    if (input.value !== limpio) {
      input.value = limpio;
    }
    this.formulario.numeroTelefono = limpio;
  }
}
