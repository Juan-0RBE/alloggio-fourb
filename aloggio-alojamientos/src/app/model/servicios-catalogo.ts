/**
 * Catálogo de servicios estándar. Lo comparten el formulario del anfitrión y el
 * filtro de búsqueda: el back filtra por nombre exacto (sin distinguir mayúsculas),
 * así que ambos lados deben usar exactamente los mismos nombres.
 */
export const SERVICIOS_CATALOGO: readonly string[] = [
  'Wi-Fi',
  'Parqueadero',
  'Piscina',
  'Cocina',
  'Aire acondicionado',
  'Lavadora',
  'Jacuzzi',
  'TV',
  'Desayuno',
  'Mascotas permitidas',
];

/** Variantes comunes que escriben los anfitriones, apuntando al nombre estándar. */
const SINONIMOS: Record<string, string> = {
  wifi: 'Wi-Fi',
  internet: 'Wi-Fi',
  parking: 'Parqueadero',
  estacionamiento: 'Parqueadero',
  parqueo: 'Parqueadero',
  aire: 'Aire acondicionado',
  television: 'TV',
  televisor: 'TV',
  mascotas: 'Mascotas permitidas',
  petfriendly: 'Mascotas permitidas',
};

/** Minúsculas, sin tildes ni signos: "Wi-Fi" → "wifi", "Televisión" → "television". */
export function claveTexto(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '');
}

/**
 * Devuelve el nombre estándar si el texto corresponde a un servicio del catálogo;
 * si no, el texto limpio (el anfitrión puede ofrecer servicios fuera del catálogo).
 */
export function normalizarServicio(nombre: string): string {
  const limpio = nombre.trim().replace(/\s+/g, ' ');
  const clave = claveTexto(limpio);
  if (!clave) return '';
  const delCatalogo = SERVICIOS_CATALOGO.find((s) => claveTexto(s) === clave);
  return delCatalogo ?? SINONIMOS[clave] ?? limpio;
}
