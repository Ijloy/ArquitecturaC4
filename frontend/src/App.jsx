import { useState } from 'react'
import { verificarContribuyente, consultarVehiculo, consultarPuntos } from './api'

/** Muestra un objeto JSON como tabla clave/valor (solo valores simples). */
function TablaDatos({ datos }) {
  if (!datos) return null
  const filas = Object.entries(datos).filter(([, v]) => v !== null && typeof v !== 'object')
  return (
    <table className="datos">
      <tbody>
        {filas.map(([k, v]) => (
          <tr key={k}><th>{k}</th><td>{String(v)}</td></tr>
        ))}
      </tbody>
    </table>
  )
}

export default function App() {
  const [correo, setCorreo] = useState('')
  const [ruc, setRuc] = useState('')
  const [placa, setPlaca] = useState('')

  const [contribuyente, setContribuyente] = useState(null)
  const [vehiculo, setVehiculo] = useState(null)
  const [licencia, setLicencia] = useState(null)
  const [errorAnt, setErrorAnt] = useState('')

  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState('')

  const paso1Valido = /\S+@\S+\.\S+/.test(correo) && /^\d{13}$/.test(ruc)

  async function verificar(e) {
    e.preventDefault()
    setError(''); setContribuyente(null); setVehiculo(null); setLicencia(null); setErrorAnt('')
    setCargando(true)
    try {
      const r = await verificarContribuyente(ruc, correo)
      setContribuyente(r)
      if (!r.esContribuyente || !r.esPersonaNatural) setError(r.mensaje)
    } catch (err) {
      setError(err.message)
    } finally {
      setCargando(false)
    }
  }

  async function buscarVehiculo(e) {
    e.preventDefault()
    setError(''); setVehiculo(null); setLicencia(null); setErrorAnt('')
    setCargando(true)
    // Las dos consultas son independientes: se lanzan en paralelo
    const [veh, lic] = await Promise.allSettled([
      consultarVehiculo(placa),
      consultarPuntos(contribuyente.cedula, placa),
    ])
    if (veh.status === 'fulfilled') setVehiculo(veh.value)
    else setError(`Vehículo: ${veh.reason.message}`)
    if (lic.status === 'fulfilled') setLicencia(lic.value)
    else setErrorAnt(lic.reason.message)
    setCargando(false)
  }

  const verificado = contribuyente?.esContribuyente && contribuyente?.esPersonaNatural

  return (
    <main>
      <h1>Consulta de contribuyente y vehículo</h1>
      <p className="sub">Sistema de Consulta Tributaria y Vehicular · SRI · ANT</p>

      <form onSubmit={verificar} className="card">
        <h2>1. Datos del contribuyente</h2>
        <label>Correo electrónico
          <input type="email" value={correo} onChange={(e) => setCorreo(e.target.value)} placeholder="nombre@correo.com" />
        </label>
        <label>RUC (persona natural)
          <input value={ruc} maxLength={13} onChange={(e) => setRuc(e.target.value.replace(/\D/g, ''))} placeholder="1712345678001" />
        </label>
        <button disabled={!paso1Valido || cargando}>Verificar en el SRI</button>
      </form>

      {error && <div className="alerta error">{error}</div>}

      {contribuyente?.datos && (
        <section className="card">
          <h2>Información del contribuyente</h2>
          <p className={verificado ? 'ok' : 'warn'}>{contribuyente.mensaje}</p>
          <TablaDatos datos={contribuyente.datos} />
        </section>
      )}

      {verificado && (
        <form onSubmit={buscarVehiculo} className="card">
          <h2>2. Matrícula del vehículo</h2>
          <label>Placa
            <input value={placa} maxLength={8} onChange={(e) => setPlaca(e.target.value.toUpperCase())} placeholder="PBA1234" />
          </label>
          <button disabled={placa.length < 5 || cargando}>Consultar vehículo y licencia</button>
        </form>
      )}

      {cargando && <p className="cargando">Consultando…</p>}

      {vehiculo && (
        <section className="card">
          <h2>Información del vehículo (SRI)</h2>
          <TablaDatos datos={vehiculo} />
        </section>
      )}

      {(licencia || errorAnt) && (
        <section className="card">
          <h2>Puntos de licencia (ANT) — cédula {contribuyente.cedula}</h2>
          {errorAnt && <div className="alerta error">{errorAnt}</div>}
          {licencia && (
            <>
              <span className={`badge ${licencia.fuente}`}>Fuente: {licencia.fuente}</span>{' '}
              <span className="badge circuito">Circuit Breaker: {licencia.estadoCircuito}</span>
              {licencia.advertencia && <div className="alerta warn">{licencia.advertencia}</div>}
              <p className="puntos">{licencia.licencia.puntos ?? '—'} <small>puntos</small></p>
              <p className="meta">Obtenido de la ANT: {new Date(licencia.licencia.obtenidoEn).toLocaleString()}</p>
              {licencia.licencia.citaciones.length > 0 && (
                <table className="datos">
                  <thead>
                    <tr>{Object.keys(licencia.licencia.citaciones[0]).map((c) => <th key={c}>{c}</th>)}</tr>
                  </thead>
                  <tbody>
                    {licencia.licencia.citaciones.map((fila, i) => (
                      <tr key={i}>{Object.values(fila).map((v, j) => <td key={j}>{v}</td>)}</tr>
                    ))}
                  </tbody>
                </table>
              )}
            </>
          )}
        </section>
      )}

    </main>
  )
}
