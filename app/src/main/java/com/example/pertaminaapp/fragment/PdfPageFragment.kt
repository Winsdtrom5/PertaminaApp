package com.example.pertaminaapp.fragment

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.pertaminaapp.R
import java.io.File

class PdfPageFragment : Fragment() {
    private lateinit var pdfImageView: ImageView
    private lateinit var pdfFile: File
    private var pageIndex: Int = 0

    companion object {
        private const val ARG_PDF_FILE_PATH = "pdf_file_path"

        fun newInstance(pdfFilePath: String): PdfPageFragment {
            val fragment = PdfPageFragment()
            val args = Bundle()
            args.putString(ARG_PDF_FILE_PATH, pdfFilePath)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pdfFile = File(arguments?.getString(ARG_PDF_FILE_PATH) ?: "")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_pdf_page, container, false)
        pdfImageView = rootView.findViewById(R.id.pdfImageView)
        displayPdfPage()
        return rootView
    }

    private fun displayPdfPage() {
        val parcelFileDescriptor: ParcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(parcelFileDescriptor)

        // Iterate through all pages in the PDF and display them
        for (i in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(i)

            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfImageView.setImageBitmap(bitmap)

            page.close()
        }

        pdfRenderer.close()
    }
}

