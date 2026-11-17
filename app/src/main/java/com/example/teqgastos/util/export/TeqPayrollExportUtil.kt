package com.example.teqgastos.util.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.teqgastos.data.local.entity.RendicionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TeqPayrollExportUtil {

    /**
     * Genera un archivo CSV compatible con la nómina bancaria chilena y SAP Business One
     */
    fun generatePayrollCsv(
        context: Context,
        rendicion: RendicionEntity,
        totalReembolsoClp: Double
    ): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Nomina_Transferencia_Rendicion_${rendicion.id}_$timestamp.csv"
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val csvFile = File(exportDir, fileName)

        FileOutputStream(csvFile).bufferedWriter().use { writer ->
            // Encabezados estándar de Nómina Bancaria SAP / Banco de Chile
            writer.write("RUT_TECNICO;NOMBRE_COLABORADOR;CENTRO_COSTO;MONTO_REEMBOLSO_CLP;TIPO_TRANSFERENCIA;BANCO_DESTINO;REFERENCIA_ERP;FECHA_GENERACION\n")

            // Fila con los datos del colaborador técnico de TEQMED
            val rut = rendicion.tecnicoRut
            val nombre = rendicion.tecnicoNombre
            val centroCosto = rendicion.centroCostoNombre
            val montoEntero = totalReembolsoClp.toLong()
            val tipoTransf = "REEMBOLSO_VIATICO_TEQMED"
            val banco = "BANCO_DE_CHILE"
            val refErp = "SAP-REND-${rendicion.id}"
            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            writer.write("$rut;$nombre;$centroCosto;$montoEntero;$tipoTransf;$banco;$refErp;$fecha\n")
        }

        return csvFile
    }

    /**
     * Comparte cualquier archivo (PDF, CSV, imagen) mediante el menú nativo ShareSheet de Android
     */
    fun shareFileWithSystem(
        context: Context,
        file: File,
        mimeType: String,
        chooserTitle: String = "Compartir Documento"
    ) {
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)
    }
}
