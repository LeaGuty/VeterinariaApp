package com.miempresa.veterinaria.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class VeterinariaProvider : ContentProvider() {

    // Simulación de datos para exponer (normalmente vendrían de una BD SQL)
    private val datosSimulados = listOf(
        arrayOf(1, "Juan Pérez", "Perro"),
        arrayOf(2, "María Silva", "Gato"),
        arrayOf(3, "Pedro Pascal", "Hamster")
    )

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        // Creamos un cursor (tabla) en memoria con columnas
        val columnas = arrayOf("_id", "cliente", "mascota")
        val cursor = MatrixCursor(columnas)

        // Llenamos el cursor con nuestros datos simulados
        for (fila in datosSimulados) {
            cursor.addRow(fila)
        }

        return cursor
    }

    // Los siguientes métodos son obligatorios pero no los usaremos en este ejemplo básico
    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.com.miempresa.veterinaria.provider.datos"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}