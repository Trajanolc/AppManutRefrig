package com.example.myapplication




import android.R.attr
import android.R.attr.*
import android.content.ContentResolver
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import com.example.myapplication.databinding.InsertFormBinding
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import kotlin.math.round


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class InsertForm : Fragment() {


    private var _binding: InsertFormBinding? = null


    val listImgsAntes : MutableList<File> = mutableListOf()
    val listImgsDepois : MutableList<File> = mutableListOf()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        _binding = InsertFormBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        val versaoequipamentos = this.activity?.getSharedPreferences("Equipamentos", MODE_PRIVATE)
        var lista0 = arrayListOf<String>("0","1")
        var lista1 = arrayListOf<String>("0","1")
        var lista2 = arrayListOf<String>("0","1")
        val arrayAdapter0 = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            lista0
        )
        val arrayAdapter1 = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            lista1
        )
        val arrayAdapter2 = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            lista2
        )
        if (versaoequipamentos != null) {
            val listaequidb = versaoequipamentos.getStringSet("listaEquipamentos", mutableSetOf("0","1"))
            lista0.clear()
            lista0.add(" ")
            listaequidb?.forEach { equip ->
                lista0.add(equip.split("_")[0])

            }
            lista0 = lista0.distinct() as ArrayList<String>
            arrayAdapter0.clear()
            arrayAdapter0.addAll(lista0)
            binding.instalacao.adapter = arrayAdapter0


        }


        binding.instalacao.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(binding.instalacao.selectedItem == " "){
                    arrayAdapter1.clear()
                    binding.local.adapter = arrayAdapter1
                    arrayAdapter2.clear()
                    binding.equipamento.adapter =arrayAdapter2
                    return
                }
                if (versaoequipamentos != null) {
                    val listaequidb =
                        versaoequipamentos.getStringSet("listaEquipamentos", mutableSetOf("0", "1"))
                    lista1.clear()
                    lista1.add(" ")
                    listaequidb?.forEach { equip ->
                        if(binding.instalacao.selectedItem.toString() == equip.split("_")[0]) {
                            lista1.add(equip.split("_")[1])
                        }
                    }
                    lista1 = lista1.distinct() as ArrayList<String>
                    arrayAdapter1.clear()
                    arrayAdapter1.addAll(lista1)
                    binding.local.adapter = arrayAdapter1

                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        binding.local.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(binding.local.selectedItem == " "){
                    arrayAdapter2.clear()
                    binding.equipamento.adapter =arrayAdapter2
                    return
                }
                if (versaoequipamentos != null) {
                    val listaequidb =
                        versaoequipamentos.getStringSet("listaEquipamentos", mutableSetOf("0", "1"))
                    lista2.clear()
                    lista2.add(" ")
                    listaequidb?.forEach { equip ->
                        if(binding.instalacao.selectedItem.toString() == equip.split("_")[0] && binding.local.selectedItem.toString() == equip.split("_")[1]) {
                            lista2.add("${equip.split("_")[2]} - ${equip.split("_")[3]}")
                        }
                    }
                    lista2 = lista2.distinct() as ArrayList<String>
                    arrayAdapter2.clear()
                    arrayAdapter2.addAll(lista2)
                    binding.equipamento.adapter = arrayAdapter2

                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }






        binding.returnHome.setOnClickListener {
            findNavController().navigate(R.id.action_insert_form_to_FirstFragment)
        }

        binding.imgAntes.setOnClickListener{
            listImgsAntes.clear()
            getContentAntes.launch("image/*")

        }

        binding.imgDepois.setOnClickListener{
            listImgsDepois.clear()
            getContentDepois.launch("image/*")
        }

        binding.finalizar.setOnClickListener{

        }

        binding.finalizar.setOnClickListener {

            //TODO enviar para um bucket S3 as fotos

            val ID = ((System.currentTimeMillis() - 1645473084517)/1000).toString()
            val pat = binding.chamado.text.toString()
            val local = binding.local.selectedItem.toString() //TODO impedir de enviar valor nulo para local
            val instalacao = binding.instalacao.selectedItem.toString()
            val equipamento = binding.equipamento.selectedItem.toString()

            val tipoManut = mutableListOf<String>()
            if(binding.switchSensitiva.isChecked) tipoManut.add("Manutenção Sensitiva")
            if(binding.switchPreventiva.isChecked) tipoManut.add("Manutenção Preventiva")
            if(binding.switchCorretiva.isChecked) tipoManut.add("Manutenção Corretiva")

            val tipoServicos = mutableListOf<String>()
            if(binding.switchLimpezaFiltros.isChecked) tipoServicos.add("Limpeza de Filtros")
            if(binding.switchRecargaDeGas.isChecked) tipoServicos.add("Recarga de Gás")
            if(binding.switchLimpezaQuimico.isChecked) tipoServicos.add("Limpeza com Químicos Bactericidas")

            val tipoTroca = mutableListOf<String>()
            if(binding.switchRele.isChecked) tipoTroca.add("Troca de Relé")
            if(binding.switchCapacitor.isChecked) tipoTroca.add("Troca de Capacitor")
            if(binding.switchFusivel.isChecked) tipoTroca.add("Troca de Fusível")
            if(binding.switchVentilador.isChecked) tipoTroca.add("Troca de Ventilador")

            val OBS = binding.OBS.text.toString()

            val DataFim = System.currentTimeMillis().toString()
            // TODO incluir id do funcionario e do equipamento
            runBlocking {
                putItemInTable(
                    pat,
                    local,
                    instalacao,
                    equipamento,
                    equipamento,
                    tipoManut,
                    tipoServicos,
                    tipoTroca,
                    OBS,
                    1.toString(),
                    DataFim
                )
//            OBS: String,
//            FuncionarioID: String,
//            DataFim: String,
            }
        }


    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun putS3Object(bucketName: String, objectKey: String, objectPath: String) {

        val metadataVal = mutableMapOf<String, String>()
        metadataVal["myVal"] = "test"

        val request = PutObjectRequest {
            var bucket = bucketName
            key = objectKey
            metadata = metadataVal
            this.body = Paths.get(objectPath).asByteStream()
        }

        S3Client { region = "us-east-2" }.use { s3 ->
            val response =s3.putObject(request)
            println("Tag information is ${response.eTag}")
        }
    }



    private val getContentAntes = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { urisAntes: List<Uri?> ->
        urisAntes.forEach { urii ->

            if (urii != null) {

                var bitmap: Bitmap? = null

                try {

                    val contentResolver: ContentResolver = this.requireContext().contentResolver

                    bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(contentResolver, urii)
                    } else {
                        val source: ImageDecoder.Source =
                            urii.let { ImageDecoder.createSource(contentResolver, it) }
                        ImageDecoder.decodeBitmap(source)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }


                // The new size we want to scale to
                val REQUIRED_SIZE = 512

                // Find the correct scale value. It should be the power of 2.
                var scale = 1
                while (bitmap!!.width / scale >= REQUIRED_SIZE &&
                    bitmap.height / scale >= REQUIRED_SIZE
                ) {
                    scale *= 2
                }


                val outputDir = requireContext().cacheDir // context being the Activity pointer

                val outputFile =
                    File.createTempFile(urii.toString().split("%").last(), ".JPEG", outputDir)



                println(bitmap.byteCount)

                val bos = ByteArrayOutputStream()
                bitmap.compress(CompressFormat.JPEG, 100, bos)
                val bitmapdata: ByteArray = bos.toByteArray()

                val fos = FileOutputStream(outputFile)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()


                runBlocking {
                    var arquivoImg = Compressor.compress(requireContext(), outputFile) {
                        resolution(bitmap!!.width/scale, bitmap!!.height/scale)
                        quality(100)
                        format(Bitmap.CompressFormat.JPEG)
                        destination(outputFile)
                    }
                }

                listImgsAntes.add(outputFile)

                binding.imgAntes.setImageURI(urisAntes[0])

            }
        }


    }

    private val getContentDepois = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { urisDepois: List<Uri?> ->
        urisDepois.forEach { urii ->

            if (urii != null) {

                var bitmap: Bitmap? = null



                try {


                    val contentResolver: ContentResolver = this.requireContext().contentResolver


                    bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(contentResolver, urii)
                    } else {
                        val source: ImageDecoder.Source =
                            urii.let { ImageDecoder.createSource(contentResolver, it) }
                        ImageDecoder.decodeBitmap(source)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }


                // The new size we want to scale to
                val REQUIRED_SIZE = 512

                // Find the correct scale value. It should be the power of 2.
                var scale = 1
                while (bitmap!!.width / scale >= REQUIRED_SIZE &&
                    bitmap!!.height / scale >= REQUIRED_SIZE
                ) {
                    scale *= 2
                }


                val outputDir = requireContext().cacheDir // context being the Activity pointer

                val outputFile =
                    File.createTempFile(urii.toString().split("%").last(), ".JPEG", outputDir)



                val bos = ByteArrayOutputStream()
                bitmap.compress(CompressFormat.JPEG, 100, bos)
                val bitmapdata: ByteArray = bos.toByteArray()

                val fos = FileOutputStream(outputFile)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()


                runBlocking {
                    var arquivoImg = Compressor.compress(requireContext(), outputFile) {
                        resolution(bitmap!!.width / scale, bitmap!!.height / scale)
                        quality(100)
                        format(Bitmap.CompressFormat.JPEG)
                        destination(outputFile)
                    }
                }


                listImgsDepois.add(outputFile)
                binding.imgDepois.setImageURI(urisDepois[0])


            }

        }
    }

    val tableNameVal = "tabela"


    private suspend fun putItemInTable(
        PAT: String,
        Local: String,
        Instalacao: String,
        Equipamento: String,
        EquipamentoID: String,
        tipoManut: List<String>,
        tipoServicos: List<String>,
        tipoTroca: List<String>,
        OBS: String,
        FuncionarioID: String,
        DataFim: String,
       // fotosAntes: List<String>,
       // fotosDepois: List<String>
        ) {
        val itemValues = mutableMapOf<String, AttributeValue>()

        val ordemID = round((System.currentTimeMillis() - 1645735671136).toDouble()).toString()

        // Add all content to the table.
        itemValues[ordemID] = AttributeValue.N(ordemID)
        itemValues[PAT] = AttributeValue.S(PAT)
        itemValues[Local] = AttributeValue.S(Local)
        itemValues[Instalacao] = AttributeValue.S(Instalacao)
        itemValues[Equipamento] = AttributeValue.S(Equipamento)
        itemValues[EquipamentoID] = AttributeValue.S(EquipamentoID)
        itemValues[tipoManut.toString()] = AttributeValue.Ss(tipoManut)
        itemValues[tipoServicos.toString()] = AttributeValue.Ss(tipoServicos)
        itemValues[tipoTroca.toString()] = AttributeValue.Ss(tipoTroca)
        itemValues[OBS] = AttributeValue.S(OBS)
        itemValues[FuncionarioID] = AttributeValue.S(FuncionarioID)
        itemValues[DataFim] = AttributeValue.S(DataFim)
       // itemValues[fotosAntes.toString()] = AttributeValue.Ss(fotosAntes)
       // itemValues[fotosDepois.toString()] = AttributeValue.Ss(fotosDepois)



        val request = PutItemRequest {
            tableName=tableNameVal
            item = itemValues
        }

        DynamoDbClient { region = "us-east-2" }.use { ddb ->
            ddb.putItem(request)
            println(" A new item was placed into $tableNameVal.")
        }
    }


    //TODO resize image before send

    //TODO




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }







}

