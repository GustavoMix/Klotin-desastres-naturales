package com.gustavomix.desastres.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private const val PREFS = "reportes"
private const val CLAVE_REPORTES = "lista"

/** Guarda los reportes de incidentes solo en este dispositivo (SharedPreferences); no hay backend todavía. */
class RepositorioReportes(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun obtenerTodos(): List<Reporte> {
        val json = prefs.getString(CLAVE_REPORTES, null) ?: return emptyList()
        val arreglo = JSONArray(json)
        return (0 until arreglo.length())
            .map { i -> arreglo.getJSONObject(i) }
            .map { obj ->
                Reporte(
                    id = obj.getString("id"),
                    tipo = obj.getString("tipo"),
                    ubicacion = obj.getString("ubicacion"),
                    descripcion = obj.getString("descripcion"),
                    fechaCreacion = obj.getLong("fechaCreacion"),
                )
            }
            .sortedByDescending { it.fechaCreacion }
    }

    fun guardar(tipo: String, ubicacion: String, descripcion: String): Reporte {
        val reporte = Reporte(
            id = UUID.randomUUID().toString(),
            tipo = tipo,
            ubicacion = ubicacion,
            descripcion = descripcion,
            fechaCreacion = System.currentTimeMillis(),
        )
        persistir(obtenerTodos() + reporte)
        return reporte
    }

    fun eliminar(id: String) {
        persistir(obtenerTodos().filterNot { it.id == id })
    }

    private fun persistir(reportes: List<Reporte>) {
        val arreglo = JSONArray()
        reportes.forEach { r ->
            arreglo.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("tipo", r.tipo)
                    put("ubicacion", r.ubicacion)
                    put("descripcion", r.descripcion)
                    put("fechaCreacion", r.fechaCreacion)
                },
            )
        }
        prefs.edit().putString(CLAVE_REPORTES, arreglo.toString()).apply()
    }
}
