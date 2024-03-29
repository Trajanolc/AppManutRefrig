package com.example.myapplication.entities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.myapplication.enum.ImgSize
import com.example.myapplication.enum.Period
import com.example.myapplication.services.S3aws
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class ListImg(val context: Context) {
    private var listBefore: ArrayList<Uri> = ArrayList(0)
    private var listAfter: ArrayList<Uri> = ArrayList(0)


    fun getListBefore(): ArrayList<Uri> {
        return listBefore
    }

    fun getListAfter(): ArrayList<Uri> {
        return listAfter
    }

    fun listsIsNotBlank(): Boolean {
        return !(listAfter.isEmpty() && listBefore.isEmpty())
    }

    private fun cargarImagenDeGaleria() {

    }


    fun addImgs(registry: ActivityResultRegistry, period: Period, img: ImageView) {




        val getContent: ActivityResultLauncher<String> = registry.register(
            "img", ActivityResultContracts.GetMultipleContents()
        ) { uris ->
            if(uris.isEmpty()) return@register
            if (period.equals(Period.BEFORE)) {
                listBefore.clear()
                listBefore.addAll(uris)
            } else {
                listAfter.clear()
                listAfter.addAll(uris)
            }
            img.setImageURI(uris[0])

        }

        getContent.launch("image/*")

    }

    fun compress() {
        //for reusing propouses
        var i = 1
        listOf(listAfter, listBefore).forEach { list ->

            //Decode uri
            val listTemp = ArrayList<Uri>(0)
            list.forEach { uri ->
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source: ImageDecoder.Source =
                        uri.let { ImageDecoder.createSource(context.contentResolver, uri) }
                    ImageDecoder.decodeBitmap(source)
                }


                // Find the correct scale value. It should be the power of 2.
                var scale = 1
                while (bitmap!!.width / scale >= ImgSize.MAX.pixels &&
                    bitmap.height / scale >= ImgSize.MAX.pixels
                ) {
                    scale *= 2
                }


                val fileName = "temp_image_${System.currentTimeMillis()}.JPEG"
                //Create Temp File
                val outputDir = context.filesDir // context being the Activity pointer
                val outputFile = File.createTempFile(fileName, null, outputDir)
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                val bitmapData: ByteArray = bos.toByteArray()
                val fos = FileOutputStream(outputFile)
                fos.write(bitmapData)
                fos.flush()
                fos.close()

                //Compress and send to outputFile adding to listBeforeTemp the uri
                runBlocking {
                    Compressor.compress(context, outputFile) {
                        resolution(bitmap.width / scale, bitmap.height / scale)
                        quality(100)
                        format(Bitmap.CompressFormat.JPEG)
                        destination(outputFile)
                        Uri.fromFile(outputFile)
                    }
                    listTemp.add(Uri.fromFile(outputFile))
                }
            }
            if(i.equals(1)) {
                this.listAfter.clear()
                this.listAfter.addAll(listTemp)
            }
            else {
                this.listBefore.clear()
                this.listBefore.addAll(listTemp)
            }
            i++
            listTemp.clear()

        }
    }

    fun sendS3bucket(local: String, keysBefore :ArrayList<String>, keysAfter: ArrayList<String>) {
        val s3 = S3aws()
        CoroutineScope(MainScope().coroutineContext).async {
            if (listBefore.isNotEmpty()) {
                var i = 0
                listBefore.forEach {
                    s3.putS3Object("imagens-refrigeracao", it, local, keysBefore[i])
                    i++
                }
            }

            if (listAfter.isNotEmpty()) {
                var i = 0
                listAfter.forEach {
                    s3.putS3Object("imagens-refrigeracao", it, local, keysAfter[i])
                    i++
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListImg

        if (context != other.context) return false
        if (listBefore != other.listBefore) return false
        if (listAfter != other.listAfter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + listBefore.hashCode()
        result = 31 * result + listAfter.hashCode()
        return result
    }
}