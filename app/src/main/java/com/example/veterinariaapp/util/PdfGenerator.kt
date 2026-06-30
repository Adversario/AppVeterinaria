package com.example.veterinariaapp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.veterinaria.data.model.Consulta
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {
    private const val PageWidth = 595
    private const val PageHeight = 842
    private const val Margin = 48

    fun generarRecetaPdf(
        context: Context,
        consulta: Consulta,
        nombreMascota: String
    ): File {
        val outputDir = context.externalCacheDir ?: context.cacheDir
        val file = File(outputDir, "receta_consulta_${consulta.id}_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PageWidth, PageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(34, 45, 64)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(45, 88, 120)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(32, 32, 32)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(95, 95, 95)
            textSize = 10f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(210, 216, 224)
            strokeWidth = 1.4f
        }

        var y = 56
        canvas.drawText("Clinica Veterinaria AppVeterinaria", Margin.toFloat(), y.toFloat(), titlePaint)
        y += 20
        canvas.drawText("Fecha de emision: ${fechaEmision()}", Margin.toFloat(), y.toFloat(), mutedPaint)
        y += 22
        canvas.drawLine(Margin.toFloat(), y.toFloat(), (PageWidth - Margin).toFloat(), y.toFloat(), linePaint)

        y += 36
        y = drawSectionTitle(canvas, "Paciente", y, headingPaint)
        y = drawParagraph(
            canvas = canvas,
            text = "Mascota: $nombreMascota\nID consulta: ${consulta.id}\nID mascota: ${consulta.mascotaId}\nMotivo de atencion: ${consulta.motivo}\nFecha consulta: ${consulta.fecha}",
            y = y,
            paint = bodyPaint
        )

        y += 18
        y = drawSectionTitle(canvas, "Clinica", y, headingPaint)
        y = drawParagraph(
            canvas = canvas,
            text = consulta.diagnostico.ifBlank { "Diagnostico pendiente de registro." },
            y = y,
            paint = bodyPaint
        )

        y += 18
        y = drawSectionTitle(canvas, "Tratamiento / Receta", y, headingPaint)
        y = drawParagraph(
            canvas = canvas,
            text = consulta.tratamiento.ifBlank { "Tratamiento pendiente de registro." },
            y = y,
            paint = bodyPaint
        )

        y = maxOf(y + 72, PageHeight - 148)
        canvas.drawLine((PageWidth - 260).toFloat(), y.toFloat(), (PageWidth - Margin).toFloat(), y.toFloat(), linePaint)
        canvas.drawText("Firma STAFF", (PageWidth - 210).toFloat(), (y + 20).toFloat(), mutedPaint)

        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun compartirRecetaPdf(
        context: Context,
        consulta: Consulta,
        nombreMascota: String,
        recipientEmail: String? = null
    ) {
        val pdf = generarRecetaPdf(context, consulta, nombreMascota)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            pdf
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Receta consulta ${consulta.id} - $nombreMascota")
            recipientEmail?.takeIf { it.isNotBlank() }?.let { email ->
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Compartir receta PDF")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (context !is Activity) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun drawSectionTitle(canvas: android.graphics.Canvas, title: String, y: Int, paint: Paint): Int {
        canvas.drawText(title.uppercase(Locale("es", "CL")), Margin.toFloat(), y.toFloat(), paint)
        return y + 18
    }

    private fun drawParagraph(
        canvas: android.graphics.Canvas,
        text: String,
        y: Int,
        paint: TextPaint
    ): Int {
        val width = PageWidth - (Margin * 2)
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(Margin.toFloat(), y.toFloat())
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height + 8
    }

    private fun fechaEmision(): String =
        SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "CL")).format(Date())
}
