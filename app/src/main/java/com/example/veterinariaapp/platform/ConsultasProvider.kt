package com.example.veterinariaapp.platform

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.veterinaria.data.Repository

class ConsultasProvider : ContentProvider() {

    companion object {
        private const val AUTH = "com.example.veterinariaapp.consultasprovider"
        private const val PATH = "consultas"
        private const val ALL = 1
        private const val ONE = 2

        val CONTENT_URI: Uri = Uri.parse("content://$AUTH/$PATH")
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTH, "consultas", ALL)
        addURI(AUTH, "consultas/*", ONE)
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        return when (uriMatcher.match(uri)) {
            ALL -> obtenerTodas()
            ONE -> obtenerPorId(uri.lastPathSegment.orEmpty())
            else -> throw IllegalArgumentException("URI no soportada: $uri")
        }
    }

    private fun obtenerTodas(): Cursor {
        val cursor = MatrixCursor(arrayOf("id", "mascotaId", "motivo", "fecha", "diagnostico", "tratamiento"))
        Repository.getConsultas().forEach { c ->
            cursor.addRow(arrayOf(c.id, c.mascotaId, c.motivo, c.fecha, c.diagnostico, c.tratamiento))
        }
        return cursor
    }

    private fun obtenerPorId(id: String): Cursor {
        val cursor = MatrixCursor(arrayOf("id", "mascotaId", "motivo", "fecha", "diagnostico", "tratamiento"))
        Repository.getConsultas().firstOrNull { it.id == id }?.let { c ->
            cursor.addRow(arrayOf(c.id, c.mascotaId, c.motivo, c.fecha, c.diagnostico, c.tratamiento))
        }
        return cursor
    }

    override fun getType(uri: Uri): String =
        when (uriMatcher.match(uri)) {
            ALL -> "vnd.android.cursor.dir/vnd.$AUTH.consulta"
            ONE -> "vnd.android.cursor.item/vnd.$AUTH.consulta"
            else -> throw IllegalArgumentException("URI no soportada: $uri")
        }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Insert no implementado en demo")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Delete no implementado en demo")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Update no implementado en demo")
    }
}
