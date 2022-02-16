package com.example.myapplication




import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import aws.smithy.kotlin.runtime.util.asyncLazy
import com.example.myapplication.databinding.InsertFormBinding

import java.io.File
import kotlin.math.round


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class InsertForm : Fragment() {


    private var _binding: InsertFormBinding? = null


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

//        val file: File = File("dados/APP_INSTALACAO.csv")
//        var listaInstalacao : MutableList<String> = ArrayList()
//        csvReader().open(file){
//            readAllAsSequence().forEach { row: List<String> ->
//                listaInstalacao.add(row[0])
//            }
//        }
//        val listaInstalacaoAdapter = ArrayAdapter<String>(this.requireActivity(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item,listaInstalacao)
//        binding.instalacao.adapter = listaInstalacaoAdapter

        binding.returnHome.setOnClickListener {
            findNavController().navigate(R.id.action_insert_form_to_FirstFragment)
        }

        binding.imgAntes.setOnClickListener{

            getContentAntes.launch("image/*")

        }

        binding.imgDepois.setOnClickListener{

            getContentDepois.launch("image/*")
        }

        binding.finalizar.setOnClickListener {
            val pat = binding.chamado.text.toString()
            val local = binding.local.selectedItem.toString() //TODO impedir de enviar valor nulo
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
            asyncLazy {
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








    private val getContentAntes = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { urisAntes: List<Uri?> ->

        binding.imgAntes.setImageURI(urisAntes.get(0))
    }

    private val getContentDepois = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { urisDepois: List<Uri?> ->
        binding.imgDepois.setImageURI(urisDepois.get(0))
    }

    val tableNameVal = "tabela"

    private val CUSTOMEPOCH = 1300000000000; // artificial epoch
    fun generateRowId(shardId:Int /* range 0-64 for shard/slot */): Double {
        var ts =  System.currentTimeMillis() - CUSTOMEPOCH; // limit to recent
        var randid = Math.floor(Math.random() * 512);
        ts = (ts * 64);   // bit-shift << 6
        ts += shardId;
        return (ts * 512) + randid;
    }

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

        val ordemID = round(generateRowId(1)).toString()

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

