package com.gustavomix.desastres.data

import android.content.Context

private const val PREFS = "avisos"
private const val CLAVE_ACTIVOS = "activos"
private const val CLAVE_SEVERIDAD = "severidad_minima"
private const val CLAVE_SOLO_MI_PAIS = "solo_mi_pais"
private const val CLAVE_PAIS = "pais"
private const val CLAVE_YA_AVISADOS = "ya_avisados"

/** País por defecto: la app se hizo para Bolivia y la mayoría va a querer eso. */
const val PAIS_POR_DEFECTO = "BO"

/**
 * Cuántos ids de eventos ya avisados se recuerdan.
 *
 * Sin este tope la lista crece para siempre en SharedPreferences. Con 1.357
 * eventos en una ventana de 14 días, 2.000 cubre de sobra: para que un id se
 * olvide y el evento vuelva a avisar, tendría que seguir en el feed después de
 * que pasaran otros 2.000 eventos por delante, y a esa altura ya salió de la
 * ventana de 14 días del feed.
 */
private const val MAXIMO_RECORDADOS = 2000

data class AjustesAvisos(
    val activos: Boolean = false,
    val severidadMinima: Severidad = Severidad.NARANJA,
    val soloMiPais: Boolean = false,
    val pais: String = PAIS_POR_DEFECTO,
)

/**
 * Ajustes de las notificaciones y memoria de lo ya avisado.
 *
 * Los avisos arrancan **apagados**: una app que empieza a vibrar sola sin que se
 * lo hayan pedido es una app que se desinstala. Se prenden desde la pantalla Más.
 */
class PreferenciasAvisos(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun leer(): AjustesAvisos = AjustesAvisos(
        activos = prefs.getBoolean(CLAVE_ACTIVOS, false),
        severidadMinima = severidadDesde(prefs.getString(CLAVE_SEVERIDAD, null)),
        soloMiPais = prefs.getBoolean(CLAVE_SOLO_MI_PAIS, false),
        pais = prefs.getString(CLAVE_PAIS, PAIS_POR_DEFECTO) ?: PAIS_POR_DEFECTO,
    )

    fun guardar(ajustes: AjustesAvisos) {
        prefs.edit()
            .putBoolean(CLAVE_ACTIVOS, ajustes.activos)
            .putString(CLAVE_SEVERIDAD, ajustes.severidadMinima.name)
            .putBoolean(CLAVE_SOLO_MI_PAIS, ajustes.soloMiPais)
            .putString(CLAVE_PAIS, ajustes.pais)
            .apply()
    }

    /** Ids ya avisados, para no repetir el mismo evento en cada chequeo. */
    fun yaAvisados(): Set<String> = prefs.getStringSet(CLAVE_YA_AVISADOS, emptySet()).orEmpty()

    fun recordarAvisados(ids: Collection<String>) {
        if (ids.isEmpty()) return
        // Los más nuevos primero: si hay que recortar, se olvidan los viejos.
        val recordados = (ids.toList() + yaAvisados()).distinct().take(MAXIMO_RECORDADOS)
        prefs.edit().putStringSet(CLAVE_YA_AVISADOS, recordados.toSet()).apply()
    }

    /**
     * Marca como avisado todo lo que hay hoy, sin notificar nada.
     *
     * Se usa al prender los avisos: si no, el primer chequeo encontraría catorce
     * días de eventos "nuevos" y dispararía decenas de notificaciones de una,
     * que es exactamente la forma de que alguien las apague para siempre.
     */
    fun marcarTodoComoVisto(ids: Collection<String>) {
        prefs.edit().putStringSet(CLAVE_YA_AVISADOS, ids.take(MAXIMO_RECORDADOS).toSet()).apply()
    }
}

private fun severidadDesde(nombre: String?): Severidad =
    Severidad.entries.firstOrNull { it.name == nombre } ?: Severidad.NARANJA

/**
 * ¿Este evento merece una notificación con estos ajustes?
 *
 * `Severidad` está declarado de mayor a menor (ROJA primero), así que "al menos
 * naranja" es un `ordinal` menor o igual, no mayor.
 */
fun mereceAviso(evento: Evento, ajustes: AjustesAvisos): Boolean {
    if (severidadDe(evento).ordinal > ajustes.severidadMinima.ordinal) return false
    if (ajustes.soloMiPais && !evento.paises.contains(ajustes.pais)) return false
    return true
}
