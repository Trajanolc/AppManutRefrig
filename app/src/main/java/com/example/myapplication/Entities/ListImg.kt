package com.example.myapplication.Entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.enum.ImgSize
import com.example.myapplication.enum.Period
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ListImg(val context: Context) {
    private var listBefore: ArrayList<Uri> = ArrayList(0)
    private var listAfter: ArrayList<Uri> = ArrayList(0)

    fun AddImgs(registry: ActivityResultRegistry, period: Period): Uri {
        val getContent: ActivityResultLauncher<String> = registry.register(
            "img", ActivityResultContracts.GetMultipleContents()
        ) { uris ->
            if (period == Period.BEFORE) {
                listBefore.clear()
                listBefore.addAll(uris)
            } else {
                listAfter.clear()
                listAfter.addAll(uris)
            }

        }
        getContent.launch("image/*")
        return if (period == Period.BEFORE) listBefore[0] else listAfter[0]
        //TODO adcionar miniatura de imagem no biding
    }

    fun Compesss() {
        //for reusing propouses
        val lists = listOf(listAfter, listBefore)
        lists.forEach { list ->

            //Decode uri
            val listTemp = ArrayList<Uri>(0)
            listBefore.forEach { uri ->
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
                    bitmap.height / scale >= ImgSize.MAX.pixels &&
                    bitmap.width / scale <= ImgSize.MIN.pixels &&
                    bitmap.height / scale <= ImgSize.MIN.pixels
                ) {
                    scale *= 2
                }

                //Create Temp File
                val outputDir = context.cacheDir // context being the Activity pointer
                val outputFile =
                    File.createTempFile(uri.toString().split("%").last(), ".JPEG", outputDir)
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
                        listTemp.add(Uri.fromFile(outputFile))
                    }
                }
            }
            list.clear()
            list.addAll(listTemp)
        }
    }
}