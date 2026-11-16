import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../service/auth.service';

type CampoTexto =
  | 'nombreUsuario' | 'correoUsuario' | 'claveUsuario' | 'confirmarClave'
  | 'primerNombre' | 'segundoNombre' | 'primerApellido' | 'segundoApellido'
  | 'documentoIdentidad' | 'numeroTelefono';

type TipoFiltro = 'letras' | 'numeros' | 'usuario' | 'sinEspacios';

// Caracteres que se ELIMINAN al escribir o pegar
const PROHIBIDOS: Record<TipoFiltro, RegExp> = {
  letras: /[^A-Za-zÁÉÍÓÚÜÑáéíóúüñ\s]/g,
  numeros: /\D/g,
  usuario: /[^A-Za-z0-9._-]/g,
  sinEspacios: /\s/g,
};

const REGEX_NOMBRE = /^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(\s[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$/;

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.scss',
})
export class Registro implements OnDestroy {
  formulario = {
    correoUsuario: '',
    nombreUsuario: '',
    claveUsuario: '',
    confirmarClave: '',
    rolUsuario: 'HUESPED',
    documentoIdentidad: '',
    primerNombre: '',
    segundoNombre: '',
    primerApellido: '',
    segundoApellido: '',
    fechaNacimiento: '',
    numeroTelefono: '',
  };

  pasoActual = 1;
  mostrarClavePrincipal = false;
  mostrarClaveConfirmar = false;
  cargando = false;
  registroExitoso = false;
  mensajeError = '';
  correoEnviado = true;


  private readonly EDAD_MINIMA = 18;
  readonly fechaMaxima: string;
  readonly fechaMinima = '1900-01-01';

  private temporizador?: ReturnType<typeof setTimeout>;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    const limite = new Date();
    limite.setFullYear(limite.getFullYear() - this.EDAD_MINIMA);
    this.fechaMaxima = this.formatear(limite);
  }

  ngOnDestroy() {
    clearTimeout(this.temporizador);
  }

  private formatear(d: Date): string {
    const mes = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mes}-${dia}`;
  }


  filtrar(evento: Event, campo: CampoTexto, tipo: TipoFiltro) {
    const input = evento.target as HTMLInputElement;
    const limpio = input.value.replace(PROHIBIDOS[tipo], '');
    input.value = limpio;
    this.formulario[campo] = limpio;
  }

  // ---------- Validaciones ----------

  private validarPaso1(): string | null {
    const f = this.formulario;

    if (!f.nombreUsuario || !f.correoUsuario || !f.claveUsuario || !f.confirmarClave) {
      return 'Completa todos los campos';
    }
    if (!/^[A-Za-z0-9._-]{4,20}$/.test(f.nombreUsuario)) {
      return 'El usuario debe tener entre 4 y 20 caracteres (letras, números, . _ -)';
    }
    if (f.correoUsuario.length > 100 || !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(f.correoUsuario)) {
      return 'Correo electrónico inválido';
    }

    const c = f.claveUsuario;
    if (c.length < 6 || c.length > 20) return 'La contraseña debe tener entre 6 y 20 caracteres';
    if (!/[A-Z]/.test(c)) return 'La contraseña debe tener una mayúscula';
    if (!/[a-z]/.test(c)) return 'La contraseña debe tener una minúscula';
    if (!/[0-9]/.test(c)) return 'La contraseña debe tener un número';
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(c)) return 'La contraseña debe tener un carácter especial (!@#$%^&*)';
    if (c !== f.confirmarClave) return 'Las contraseñas no coinciden';

    return null;
  }

  private validarPaso2(): string | null {
    const f = this.formulario;

    if (!f.primerNombre.trim() || !f.primerApellido.trim()) {
      return 'Primer nombre y primer apellido son obligatorios';
    }
    for (const n of [f.primerNombre, f.segundoNombre, f.primerApellido, f.segundoApellido]) {
      const v = n.trim();
      if (v && (v.length < 2 || !REGEX_NOMBRE.test(v))) {
        return 'Los nombres y apellidos solo pueden contener letras (mínimo 2)';
      }
    }
    if (!/^\d{6,10}$/.test(f.documentoIdentidad)) {
      return 'El documento debe tener entre 6 y 10 dígitos';
    }
    if (!/^\d{10}$/.test(f.numeroTelefono)) {   // formato Colombia; ajusta si aceptas otros países
      return 'El teléfono debe tener 10 dígitos';
    }
    if (!f.fechaNacimiento) return 'Ingresa tu fecha de nacimiento';
    if (f.fechaNacimiento < this.fechaMinima || f.fechaNacimiento > this.fechaMaxima) {
      return `Debes ser mayor de ${this.EDAD_MINIMA} años`;
    }
    return null;
  }

  // ---------- Navegación y envío ----------

  siguientePaso() {
    const error = this.validarPaso1();
    if (error) {
      this.mensajeError = error;
      return;
    }
    this.mensajeError = '';
    this.pasoActual = 2;
  }

  volver() {
    this.mensajeError = '';
    this.pasoActual = 1;
  }

  alRegistrar() {
    // Enter en el paso 1 no debe enviar el formulario
    if (this.pasoActual === 1) {
      this.siguientePaso();
      return;
    }
    if (this.cargando) return;

    const error = this.validarPaso2();
    if (error) {
      this.mensajeError = error;
      return;
    }

    this.mensajeError = '';
    this.cargando = true;

    const { confirmarClave, ...datos } = this.formulario;
    const payload = {
      ...datos,
      nombreUsuario: datos.nombreUsuario.trim(),
      correoUsuario: datos.correoUsuario.trim().toLowerCase(),
      primerNombre: datos.primerNombre.trim(),
      segundoNombre: datos.segundoNombre.trim(),
      primerApellido: datos.primerApellido.trim(),
      segundoApellido: datos.segundoApellido.trim(),
    };

    this.authService.register(payload).subscribe({
      next: (respuesta: string) => {
        this.cargando = false;
        this.correoEnviado = !respuesta.includes('no se pudo enviar');
        this.registroExitoso = true;
        this.cdr.detectChanges();
        this.temporizador = setTimeout(() => this.router.navigate(['/login']), 5000);
      },
      error: (err) => {
        this.cargando = false;
        if (err.status === 0) {
          this.mensajeError = 'No hay conexión con el servidor.';
        } else if (typeof err.error === 'string' && err.error) {
          this.mensajeError = err.error;   // 400 / 409 con el motivo
        } else {
          this.mensajeError = 'Error al registrar el usuario. Intenta de nuevo.';
        }
        this.cdr.detectChanges();
      },
    });
  }

  irALogin() {
    this.router.navigate(['/login']);
  }
}
