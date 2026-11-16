import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

type TipoResultado = 'exito' | 'rechazado' | 'no-disponible' | 'pasarela' | 'conflicto';

interface ConfigResultado {
  icono: string;
  tono: 'exito' | 'error' | 'aviso';
  titulo: string;
  descripcion: string;
}

const RESULTADOS: Record<TipoResultado, ConfigResultado> = {
  exito: {
    icono: 'pi-check-circle',
    tono: 'exito',
    titulo: '¡Tu reserva está confirmada!',
    descripcion: 'Guarda tu código de reserva. Disfruta de tu estancia en Aloggio.',
  },
  rechazado: {
    icono: 'pi-times-circle',
    tono: 'error',
    titulo: 'No pudimos procesar tu pago',
    descripcion: 'No se hizo ningún cobro. Puedes intentarlo con otra tarjeta.',
  },
  'no-disponible': {
    icono: 'pi-calendar-times',
    tono: 'aviso',
    titulo: 'Ese alojamiento ya no está disponible',
    descripcion: 'Alguien reservó esas fechas antes que tú. No se hizo ningún cobro.',
  },
  pasarela: {
    icono: 'pi-exclamation-triangle',
    tono: 'aviso',
    titulo: 'El servicio de pagos no responde',
    descripcion: 'No se hizo ningún cobro. Espera un momento e inténtalo de nuevo.',
  },
  conflicto: {
    icono: 'pi-exclamation-triangle',
    tono: 'aviso',
    titulo: 'Tu reserva necesita revisión',
    descripcion: 'Revisa tu historial de reservas o contacta a soporte antes de volver a pagar.',
  },
};

@Component({
  selector: 'app-resultado-reserva',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './resultado-reserva.html',
  styleUrl: './resultado-reserva.scss',
})
export class ResultadoReserva implements OnInit {
  tipo: TipoResultado = 'exito';
  config: ConfigResultado = RESULTADOS['exito'];

  reserva = {
    idReserva: 1042,
    fechaLlegada: '10 de Diciembre, 2026',
    fechaSalida: '15 de Diciembre, 2026',
    noches: 5,
    total: 2835000,
  };

  alojamiento = {
    titulo: 'Villa Moderna Vista al Valle y Montaña',
    ubicacion: 'Medellín, Antioquia',
    imagenes: [
      {
        urlImagen:
          'https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80',
      },
    ],
  };

  ngOnInit(): void {
    this.tipo = 'exito';
    this.config = RESULTADOS[this.tipo];
  }
}
