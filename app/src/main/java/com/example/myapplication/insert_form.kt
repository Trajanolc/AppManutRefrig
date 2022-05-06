package com.example.myapplication




import android.R.attr.*
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import aws.sdk.kotlin.runtime.auth.credentials.Credentials
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class InsertForm : Fragment() {


    private var _binding: InsertFormBinding? = null


    var listImgsAntes : MutableList<File> = mutableListOf()
    var listImgsDepois : MutableList<File> = mutableListOf()


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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        listImgsDepois.clear()
        listImgsAntes.clear()





        val versaoequipamentos = this.activity?.getSharedPreferences("Equipamentos", MODE_PRIVATE)
        var lista0 = arrayListOf<String>(" ")
        var lista1 = arrayListOf<String>(" ")
        var lista2 = arrayListOf<String>(" ")
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
            val listaequidb =
                versaoequipamentos.getStringSet("listaEquipamentos", mutableSetOf("0", "1"))
            lista0.clear()
            lista0.add("Selecione uma Instalação")
            listaequidb?.forEach { equip ->
                lista0.add(equip.split("_")[0])

            }
            lista0 = lista0.distinct() as ArrayList<String>
            arrayAdapter0.clear()
            arrayAdapter0.addAll(lista0)
            binding.instalacao.adapter = arrayAdapter0

        }


        binding.instalacao.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (binding.instalacao.selectedItem == "Selecione uma Instalação") {
                    arrayAdapter1.clear()
                    binding.local.adapter = arrayAdapter1
                    arrayAdapter2.clear()
                    binding.equipamento.adapter = arrayAdapter2
                    return
                }
                if (versaoequipamentos != null) {
                    val listaequidb =
                        versaoequipamentos.getStringSet("listaEquipamentos", mutableSetOf("0", "1"))
                    lista1.clear()
                    lista1.add(" ")
                    listaequidb?.forEach { equip ->
                        if (binding.instalacao.selectedItem.toString() == equip.split("_")[0]) {
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

        binding.local.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (binding.local.selectedItem == " ") {
                    arrayAdapter2.clear()
                    binding.equipamento.adapter = arrayAdapter2
                    return
                }
                if (versaoequipamentos != null) {
                    val listaequidb =
                        versaoequipamentos.getStringSet("listaEquipamentos", mutableSetOf("0", "1"))
                    lista2.clear()
                    lista2.add(" ")
                    listaequidb?.forEach { equip ->
                        if (binding.instalacao.selectedItem.toString() == equip.split("_")[0] && binding.local.selectedItem.toString() == equip.split(
                                "_"
                            )[1]
                        ) {
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

        binding.imgAntes.setOnClickListener {
            listImgsAntes.clear()
            getContentAntes.launch("image/*")

        }

        binding.imgDepois.setOnClickListener {
            listImgsDepois.clear()
            getContentDepois.launch("image/*")
        }



        binding.finalizar.setOnClickListener {

            // enviar para um bucket S3 as fotos




            if (binding.equipamento.selectedItem == null) {

                binding.scroll.fullScroll(binding.scroll.top)
                Toast.makeText(
                    requireContext(),
                    "Por favor, preencha o equipamento.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }else if (binding.equipamento.selectedItem.toString() == " "){
                binding.scroll.fullScroll(binding.scroll.top)
                Toast.makeText(
                    requireContext(),
                    "Por favor, preencha o equipamento.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if ((binding.switchSensitiva.isChecked == binding.switchPreventiva.isChecked && binding.switchPreventiva.isChecked == binding.switchCorretiva.isChecked && binding.switchCorretiva.isChecked == false)) {

                binding.scroll.fullScroll(binding.scroll.top)
                Toast.makeText(
                    requireContext(),
                    "Por favor, selecione ao menos uma manutenção.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }




            var cancelar = false

            if (listImgsDepois.isEmpty()) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Sem imagens")
                builder.setMessage("Não existem imagens após o serviço, deseja continuar?")
                builder.setPositiveButton("Não") { _, _ ->
                    Toast.makeText(context, "Envio cancelado.", Toast.LENGTH_SHORT).show()
                    cancelar = true
                }
                builder.setNegativeButton("Sim") { _, _ ->
                    cancelar = false
                    inserir()

                }
                builder.show()
                if (cancelar) {

                    return@setOnClickListener
                }
            }
            else{
                inserir()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun inserir(){
//         TODO editar o put pra inserir na DB

        val listKeysImgAntes: MutableList<String> = mutableListOf()
            var i = 0
            listImgsAntes.forEach{ arquivo ->
                i += 1
                val nomeArquivo = "${binding.equipamento.selectedItem.toString().split("-").last()}_" +
                        "${SimpleDateFormat("dd-MM-yyyy").format(Date())}_Antes_" +
                        "${i.toString().padStart(2,'0')}.JPEG".replace("/","-")

                val localArquivo = arquivo.absolutePath

                val meta = binding.equipamento.selectedItem.toString()

                runBlocking { launch{listKeysImgAntes.add( putS3Object(nomeArquivo,localArquivo,meta) ) }}
            }


            var listKeysImgDepois: MutableList<String> = mutableListOf()
            i = 0
            listImgsDepois.forEach{ arquivo ->
                i += 1
                var nomeArquivo = "${binding.equipamento.selectedItem.toString().split("-").last()}_" +
                        "${SimpleDateFormat("dd-MM-yyyy").format(Date())}_Depois_" +
                        "${i.toString().padStart(2,'0')}.JPEG".replace("/","-")

                var localArquivo = arquivo.absolutePath

                var meta = binding.equipamento.selectedItem.toString()

                runBlocking {listKeysImgDepois.add( putS3Object(nomeArquivo,localArquivo,meta) ) }
            }


            val ID = ((System.currentTimeMillis() - 1645473084517)/1000).toString()
            val pat = binding.chamado.text.toString()
            val local = binding.local.selectedItem.toString()
            val instalacao = binding.instalacao.selectedItem.toString()
            val equipamento = binding.equipamento.selectedItem.toString()

            val tipoManut = mutableListOf<String>()
            if(binding.switchSensitiva.isChecked) tipoManut.add("Manutenção Sensitiva")
            if(binding.switchPreventiva.isChecked) tipoManut.add("Manutenção Preventiva")
            if(binding.switchCorretiva.isChecked) tipoManut.add("Manutenção Corretiva")

            val tipoServicos = mutableListOf<String>()
            if(binding.switchRecargaDeGas.isChecked) tipoServicos.add("Recarga de Gás")
            if(binding.switchLimpezaFiltros.isChecked) tipoServicos.add("Limpeza de Filtros")
            if(binding.switchLimpezaQuimico.isChecked) tipoServicos.add("Limpeza Geral com Químicos Bactericidas")
            if(binding.switchDreno.isChecked) tipoServicos.add("Limpeza e desobstrução de Dreno")
            if(binding.switchControle.isChecked) tipoServicos.add("Ajuste no controle remoto")
            if(binding.switchRele.isChecked) tipoServicos.add("Ajuste na infra elétrica local")

            val tipoTroca = mutableListOf<String>()
            if(binding.switchSensorTemperatura.isChecked) tipoTroca.add("Troca de Sensor de Temperatura")
            if(binding.switchSensorDegelo.isChecked) tipoTroca.add("Troca de Sensor de Degelo")
            if(binding.switchPlaca.isChecked) tipoTroca.add("Troca de Placa de Comando")
            if(binding.switchVentiladorEvap.isChecked) tipoTroca.add("Troca de Ventilador da Evaporadora")
            if(binding.switchVentiladorCond.isChecked) tipoTroca.add("Troca de Ventilador da Condensadora")
            if(binding.switchSerpentina.isChecked) tipoTroca.add("Troca de Serpentina")
            if(binding.switchCompressor.isChecked) tipoTroca.add("Troca de Compressor")
            if(binding.switchFusivel.isChecked) tipoTroca.add("Troca de Fusível")
            if(binding.switchCapacitor.isChecked) tipoTroca.add("Troca de Capacitor")
            if(binding.switchRele.isChecked) tipoTroca.add("Troca de Relé")

            if(tipoTroca.isEmpty()&&tipoServicos.isEmpty()&&binding.OBS.text.toString()==""){
                binding.scroll.fullScroll(binding.scroll.top)
                Toast.makeText(
                    requireContext(),
                    "Por favor, selecione ao menos um serviço, material ou observação.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val OBS = binding.OBS.text.toString()
            println("Enviado")
            val DataFim = System.currentTimeMillis().toString()
            val login = requireActivity().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
            val FuncionarioID = login.getString("login","")



            var lista2 = arrayListOf<String>(" ")
            val versaoequipamentos = this.activity?.getSharedPreferences("Equipamentos", MODE_PRIVATE)
            val listaequidb =
                versaoequipamentos!!.getStringSet("listaEquipamentos", mutableSetOf("0", "1"))
            lista2.clear()
            listaequidb?.forEach { equip ->
                if (binding.instalacao.selectedItem.toString() == equip.split("_")[0] && binding.local.selectedItem.toString() == equip.split(
                        "_")[1])
                {
                    lista2.add("${equip.split("_")[2]} - ${equip.split("_")[3]}")
                }
            }
            lista2.remove(binding.equipamento.selectedItem.toString())
            println(lista2)
            val listaEquips : Array<String> = lista2.toTypedArray()
            println(listaEquips)
            val listaEquipsBol = BooleanArray(listaEquips.size){false}
            val builder = AlertDialog.Builder(requireContext())
            val listener : DialogInterface.OnMultiChoiceClickListener = DialogInterface.OnMultiChoiceClickListener {_,i,boolean ->
                listaEquipsBol.set(i,boolean)
            }
            println(listaEquipsBol.toString())
            println(listaEquips.toString())
            builder.setTitle("Deseja replicar para outra máquina no mesmo local?")

            builder.setMultiChoiceItems(listaEquips, listaEquipsBol,listener)
            builder.setPositiveButton("Replicar") {_,_->
                for(i in listaEquips.indices) {
                    if (listaEquipsBol[i] == true)

                        runBlocking {
                            launch {
                                putItemInTable(
                                    ((System.currentTimeMillis() - 1645473084517)/1000).toString(),
                                    pat,
                                    local,
                                    instalacao,
                                    listaEquips[i],
                                    tipoManut,
                                    tipoServicos,
                                    tipoTroca,
                                    OBS,
                                    FuncionarioID!!,
                                    DataFim,
                                    listKeysImgAntes,
                                    listKeysImgDepois
                                )
                            }
                        }
                    }
            }
            builder.setNegativeButton("Não Replicar"){_,_->}
            builder.show()



            runBlocking{
                launch {
                putItemInTable(
                    ((System.currentTimeMillis() - 1645473084517)/1000).toString(),
                    pat,
                    local,
                    instalacao,
                    equipamento,
                    tipoManut,
                    tipoServicos,
                    tipoTroca,
                    OBS,
                    FuncionarioID!!,
                    DataFim,
                    listKeysImgAntes,
                    listKeysImgDepois
                )
            }
            }



    }





    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun putS3Object( objectKey: String, objectPath: String, meta: String): String {
        val bucketName = "imagens-refrigeracao"
        val metadataVal = mutableMapOf<String, String>()
        metadataVal["Local"] = meta

        val request = PutObjectRequest {
            bucket = bucketName
            key = objectKey
            metadata = metadataVal
            this.body = Paths.get(objectPath).asByteStream()
        }

        S3Client { region = "us-east-2"
            credentialsProvider= StaticCredentialsProvider(Credentials("AKIAXJ6IWE3BPJLVFANI","MFr8G6u2JsoYzPLxtSjt3bgE2lVL4qKoZ0NBwOpT"))
        }.use { s3 ->
            val response =s3.putObject(request)

            if(response.eTag != null){return objectKey}
        }
        return ""
    }

    private suspend fun putItemInTable(
        ID: String,
        PAT: String,
        Local: String,
        Instalacao: String,
        Equipamento: String,
        tipoManut: List<String>,
        tipoServicos: List<String>,
        tipoTroca: List<String>,
        OBS: String,
        FuncionarioID: String,
        DataFim: String,
        fotosAntes: List<String>,
        fotosDepois: List<String>
    ) {
        val itemValues = mutableMapOf<String, AttributeValue>()


        // Add all content to the table.
        itemValues["ordemID"] = AttributeValue.N(ID)
        itemValues["PAT"] = AttributeValue.S(PAT)
        itemValues["Local"] = AttributeValue.S(Local)
        itemValues["Instalacao"] = AttributeValue.S(Instalacao)
        itemValues["Equipamento"] = AttributeValue.S(Equipamento)
        itemValues["tipoManut"] = AttributeValue.Ss(tipoManut)
        if(tipoServicos.isNotEmpty()) itemValues["tipoServicos"] = AttributeValue.Ss(tipoServicos)
        if(tipoTroca.isNotEmpty()) itemValues["tipoTroca"] = AttributeValue.Ss(tipoTroca)
        itemValues["OBS"] = AttributeValue.S(OBS)
        itemValues["FuncionarioID"] = AttributeValue.S(FuncionarioID)
        itemValues["DataFim"] = AttributeValue.S(DataFim)
        if(fotosAntes.isNotEmpty()) itemValues["fotosAntes"] = AttributeValue.Ss(fotosAntes)
        if(fotosDepois.isNotEmpty()) itemValues["fotosDepois"] = AttributeValue.Ss(fotosDepois)



        val request = PutItemRequest {

            tableName=tableNameVal
            item = itemValues
        }

        DynamoDbClient { region="us-east-2"
            credentialsProvider=
                StaticCredentialsProvider(Credentials("AKIAXJ6IWE3BPJLVFANI","MFr8G6u2JsoYzPLxtSjt3bgE2lVL4qKoZ0NBwOpT")) }.use { ddb ->

            ddb.putItem(request)

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Sucesso")
            builder.setMessage("Ordem Nº ${ID} referente ao ${Equipamento} enviada com sucesso!")
            builder.setPositiveButton("Ok") { _, _ ->
                try {
                    findNavController().navigate(R.id.action_insert_form_to_FirstFragment)
                }catch (e: IllegalArgumentException){
                    //Do nothing
                }

            }
            builder.show()
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
                    Compressor.compress(requireContext(), outputFile) {
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
                    Compressor.compress(requireContext(), outputFile) {
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

    val tableNameVal = "ordemServico"





    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }







}

