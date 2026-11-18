// El frontend envia todas las solicitudes HTTPS al API Gateway
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

async function get(path) {
  const resp = await fetch(`${API_URL}${path}`)
  const body = await resp.json().catch(() => ({}))
  if (!resp.ok) throw new Error(body.error || (resp.status === 404 ? 'No encontrado' : `Error HTTP ${resp.status}`))
  return body
}

const q = encodeURIComponent

export const verificarContribuyente = (ruc, correo) =>
  get(`/api/contribuyentes/${q(ruc)}?correo=${q(correo)}`)

export const consultarVehiculo = (placa) =>
  get(`/api/vehiculos/${q(placa)}`)

export const consultarPuntos = (cedula, placa = '') =>
  get(`/api/licencias/${q(cedula)}?placa=${q(placa)}`)
