package com.pablozavfer.cuadernopredicas.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.util.Dates
import java.io.OutputStream

/**
 * Genera un PDF A4 con el título, la referencia y las secciones de la prédica.
 * Los párrafos largos se dividen entre páginas línea por línea y los encabezados
 * no quedan solos al final de una página. Se hacen dos pasadas para numerar "Página X de N".
 */
object SermonPdfWriter {
    private const val PAGE_WIDTH = 595 // A4 en puntos (1/72 de pulgada)
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 56f
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - MARGIN - 14f
    private const val FOOTER_BASELINE = PAGE_HEIGHT - 32f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

    private val green = Color.rgb(0x1E, 0x4D, 0x3A)
    private val gold = Color.rgb(0xB0, 0x8D, 0x3A)
    private val ink = Color.rgb(0x1F, 0x1B, 0x16)
    private val muted = Color.rgb(0x5A, 0x52, 0x46)

    private val serif = Typeface.SERIF
    private val serifBold = Typeface.create(Typeface.SERIF, Typeface.BOLD)

    private fun paint(size: Float, typeface: Typeface, color: Int) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.typeface = typeface
            this.color = color
        }

    private val titlePaint = paint(22f, serifBold, green)
    private val subtitlePaint = paint(12.5f, Typeface.create(Typeface.SERIF, Typeface.ITALIC), muted)
    private val headingPaint = paint(14f, serifBold, green)
    private val pointPaint = paint(12.5f, serifBold, ink)
    private val bodyPaint = paint(11.5f, serif, ink)
    private val footerPaint = paint(9f, serif, muted).apply { textAlign = Paint.Align.CENTER }
    private val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; strokeWidth = 1.2f }

    private sealed interface Block {
        data class Text(
            val text: String,
            val paint: TextPaint,
            val spaceBefore: Float = 0f,
            val spaceAfter: Float = 0f,
            val indent: Float = 0f,
            val keepWithNext: Boolean = false,
        ) : Block

        data object Rule : Block
    }

    fun write(sermon: Sermon, out: OutputStream) {
        val blocks = buildBlocks(sermon)
        val totalPages = Pager(null, 0, sermon.displayTitle).run(blocks)
        val document = PdfDocument()
        try {
            Pager(document, totalPages, sermon.displayTitle).run(blocks)
            document.writeTo(out)
        } finally {
            document.close()
        }
    }

    private fun buildBlocks(s: Sermon): List<Block> = buildList {
        add(Block.Text(s.displayTitle, titlePaint, spaceAfter = 6f))
        if (s.scripture.isNotBlank()) add(Block.Text(s.scripture.trim(), subtitlePaint, spaceAfter = 2f))
        s.preachDate?.let { add(Block.Text(Dates.long(it), subtitlePaint)) }
        add(Block.Rule)
        s.sections().forEach { section ->
            add(Block.Text(section.heading, headingPaint, spaceBefore = 16f, spaceAfter = 6f, keepWithNext = true))
            if (section.body.isNotEmpty()) add(Block.Text(section.body, bodyPaint))
            section.points.forEachIndexed { i, point ->
                add(
                    Block.Text(
                        pointHeading(i, point), pointPaint,
                        spaceBefore = if (i == 0) 0f else 10f, spaceAfter = 3f, keepWithNext = point.body.isNotBlank(),
                    ),
                )
                if (point.body.isNotBlank()) add(Block.Text(point.body.trim(), bodyPaint, indent = 14f))
            }
        }
    }

    /** Recorre los bloques; si [document] es null solo cuenta páginas. */
    private class Pager(
        private val document: PdfDocument?,
        private val totalPages: Int,
        private val title: String,
    ) {
        private var page: PdfDocument.Page? = null
        private var pageNumber = 0
        private var y = MARGIN

        private val atTop get() = y <= MARGIN
        private val remaining get() = CONTENT_BOTTOM - y

        fun run(blocks: List<Block>): Int {
            newPage()
            blocks.forEach { block ->
                when (block) {
                    is Block.Text -> drawText(block)
                    Block.Rule -> drawRule()
                }
            }
            finishPage()
            return pageNumber
        }

        private fun newPage() {
            finishPage()
            pageNumber++
            page = document?.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            y = MARGIN
        }

        private fun finishPage() {
            val current = page ?: return
            val footer = if (totalPages > 0) "$title — Página $pageNumber de $totalPages" else ""
            current.canvas.drawText(ellipsize(footer), PAGE_WIDTH / 2f, FOOTER_BASELINE, footerPaint)
            document?.finishPage(current)
            page = null
        }

        private fun ellipsize(text: String): String {
            if (footerPaint.measureText(text) <= CONTENT_WIDTH) return text
            var cut = text
            while (cut.length > 4 && footerPaint.measureText("$cut…") > CONTENT_WIDTH) cut = cut.dropLast(1)
            return "$cut…"
        }

        private fun drawRule() {
            if (remaining < 20f) newPage()
            y += 10f
            page?.canvas?.drawLine(MARGIN, y, MARGIN + CONTENT_WIDTH, y, rulePaint)
            y += 4f
        }

        private fun drawText(block: Block.Text) {
            val width = (CONTENT_WIDTH - block.indent).toInt()
            val layout = StaticLayout.Builder
                .obtain(block.text, 0, block.text.length, block.paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(false)
                .build()

            if (!atTop) y += block.spaceBefore
            // Un encabezado necesita espacio para sí mismo y al menos unas líneas del texto siguiente.
            if (block.keepWithNext && !atTop && remaining < layout.height + bodyPaint.textSize * 4) newPage()

            var start = 0
            while (start < layout.lineCount) {
                val top = layout.getLineTop(start)
                var end = start
                while (end < layout.lineCount && layout.getLineBottom(end) - top <= remaining) end++
                if (end == start) {
                    if (!atTop) {
                        newPage()
                        continue
                    }
                    end = start + 1 // línea más alta que la página: se dibuja igual
                }
                val bottom = layout.getLineBottom(end - 1)
                page?.canvas?.let { drawLines(it, layout, block.indent, top, bottom) }
                y += bottom - top
                start = end
                if (start < layout.lineCount) newPage()
            }
            y += block.spaceAfter
        }

        private fun drawLines(canvas: Canvas, layout: StaticLayout, indent: Float, top: Int, bottom: Int) {
            canvas.save()
            canvas.translate(MARGIN + indent, y - top)
            canvas.clipRect(0f, top.toFloat(), layout.width.toFloat(), bottom.toFloat())
            layout.draw(canvas)
            canvas.restore()
        }
    }
}
