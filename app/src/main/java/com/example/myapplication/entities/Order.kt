package com.example.myapplication.entities

import android.content.Context
import android.content.DialogInterface
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.InsertForm
import com.example.myapplication.R
import com.example.myapplication.exceptions.OrderException
import com.example.myapplication.services.DynamoAws
import com.example.myapplication.services.httpServices
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


data class Order(
    var pat: String,
    val local: String,
    val plant: String,
    var equipment: String,
    val obs: String,
    val listImg: ListImg,
    val context: Context
) {
    init {
        if (equipment.isBlank()) throw OrderException("Por favor, selecione o equipamento.")
    }

    internal val employeeId = context.getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
        .getString("login", "")
    internal val dateEnd = System.currentTimeMillis().toString()
    internal var id: String = ((System.currentTimeMillis() - 1645473084517) / 1000).toString()

    private var typeManut: ArrayList<String> = ArrayList(0)
    private var typeServices: ArrayList<String> = ArrayList(0)
    private var typeSwap: ArrayList<String> = ArrayList(0)
    private var imgKeysBefore: ArrayList<String> = ArrayList(0)
    private var imgKeysAfter: ArrayList<String> = ArrayList(0)

    var gasKG: String = "0.0"

    fun getTypeManut(): ArrayList<String> {
        return typeManut
    }

    fun getTypeServices(): ArrayList<String> {
        return if (typeServices.isNotEmpty()) typeServices else arrayListOf("Troca de componentes")
    }

    fun getTypeSwap(): ArrayList<String> {
        return if (typeSwap.isNotEmpty()) typeSwap else arrayListOf("Não foram realizadas trocas")
    }

    fun getImgKeysBefore(): ArrayList<String> {
        return imgKeysBefore
    }

    fun getImgKeysAfter(): ArrayList<String> {
        return imgKeysAfter
    }


    //Setters
    fun setTypeManut(sensitiva: Boolean, preventiva: Boolean, corretiva: Boolean, subst: Boolean) {
        typeManut.clear()
        if (sensitiva) typeManut.add("Manutenção Sensitiva")
        if (preventiva) typeManut.add("Manutenção Preventiva")
        if (corretiva) typeManut.add("Manutenção Corretiva")
        if (subst) typeManut.add("Substituição de Equipamento")
        if (typeManut.isEmpty()) throw OrderException("Por favor, selecione ao menos um tipo de manutenção.")
    }

    fun setTypeServices(
        gas: Boolean,
        filtro: Boolean,
        limpGeral: Boolean,
        dreno: Boolean,
        controle: Boolean,
        eletrica: Boolean
    ) {
        typeServices.clear()
        if (gas) typeServices.add("Recarga de Gás")
        if (filtro) typeServices.add("Limpeza de Filtros")
        if (limpGeral) typeServices.add("Limpeza Geral com Químicos Bactericidas")
        if (dreno) typeServices.add("Limpeza e desobstrução de Dreno")
        if (controle) typeServices.add("Ajuste no controle remoto")
        if (eletrica) typeServices.add("Ajuste na infra elétrica local")
    }

    fun setTypeSwap(
        sensorTemp: Boolean,
        sensorDegelo: Boolean,
        placa: Boolean,
        ventEvap: Boolean,
        ventCond: Boolean,
        serpentina: Boolean,
        compressor: Boolean,
        fusivel: Boolean,
        capacit: Boolean,
        contac: Boolean,
        placaRecep: Boolean,
        motor: Boolean,
        turbina: Boolean,
        filtroBebedouro: Boolean,
        expansor: Boolean
    ) {
        if (sensorTemp) typeSwap.add("Troca de Sensor de Temperatura")
        if (sensorDegelo) typeSwap.add("Troca de Sensor de Degelo")
        if (placa) typeSwap.add("Troca de Placa de Comando")
        if (ventEvap) typeSwap.add("Troca de Ventilador da Evaporadora")
        if (ventCond) typeSwap.add("Troca de Ventilador da Condensadora")
        if (serpentina) typeSwap.add("Troca de Serpentina")
        if (compressor) typeSwap.add("Troca de Compressor")
        if (fusivel) typeSwap.add("Troca de Fusível")
        if (capacit) typeSwap.add("Troca de Capacitor")
        if (contac) typeSwap.add("Troca de Contactora")
        if (placaRecep) typeSwap.add("Troca de Placa Receptora")
        if (motor) typeSwap.add("Troca de Motor")
        if (turbina) typeSwap.add("Troca de Turbina")
        if (filtroBebedouro) typeSwap.add("Troca de Filtro de Bebedouro")
        if (expansor) typeSwap.add("Troca de Expansor")

        if (typeSwap.isEmpty() && typeManut.isEmpty()) throw OrderException("Por favor, selecione ao menos um tipo de serviço ou troca de material.")
    }

    fun setNumericInfo(
        gas: Boolean,
        gasKGInput: String
    ) {
        if (gas) {
            gasKG = gasKGInput
        }
    }


    fun generateImgKeys() {
        var i = 0
        imgKeysBefore.clear()
        listImg.getListBefore().forEach { _ ->
            i++
            imgKeysBefore.add(
                "${equipment.split("-").last().trimStart()}_" +
                        "${SimpleDateFormat("dd-MM-yyyy").format(Date())}_Antes_" +
                        "${i.toString().padStart(2, '0')}.JPEG".replace("/", "-")
            )
        }

        i = 0
        imgKeysAfter.clear()
        listImg.getListAfter().forEach { _ ->
            i++
            imgKeysAfter.add(
                "${equipment.split("-").last().trimStart()}_" +
                        "${SimpleDateFormat("dd-MM-yyyy").format(Date())}_Depois_" +
                        "${i.toString().padStart(2, '0')}.JPEG".replace("/", "-")
            )
        }

    }

    //Checks if has images or the user confirms there's no images to attach, otherwise cancel the insert.
    fun checkImagesAndInsert(fragment: InsertForm) {

         if (listImg.listsIsNotBlank()) insert(fragment) else checkImages(fragment)

    }

    private fun checkImages(fragment: InsertForm) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Sem imagens")
        builder.setMessage("Não existem imagens anexas, deseja continuar?")
        builder.setPositiveButton("Não") { _, _ ->
            fragment.goUp("Envio cancelado.")
        }
        builder.setNegativeButton("Sim") { _, _ -> insert(fragment) }
        builder.show()
    }


    private fun insert(fragment: InsertForm) {


        listImg.compress()
        generateImgKeys()
        listImg.sendS3bucket(local, imgKeysBefore, imgKeysAfter)
        fragment.goHome()

        CoroutineScope(MainScope().coroutineContext).launch {
            httpServices.addOrder(this@Order.serialize())
        }

        replicateOrder()

    }

    private fun replicateOrder() {
        val arrayEquipsReplicate = ListEquip(context).getNearEquips(plant, local)
        arrayEquipsReplicate.remove(equipment)
        if (arrayEquipsReplicate.isEmpty()) return
        val checkedEquipsReplicate = BooleanArray(arrayEquipsReplicate.size)


        val builder = AlertDialog.Builder(context)
        val listener = DialogInterface.OnMultiChoiceClickListener { _, i, isChecked ->
            checkedEquipsReplicate[i] = isChecked
        }

        builder.setTitle("Deseja replicar para outra máquina no mesmo local?")

        builder.setMultiChoiceItems(
            arrayEquipsReplicate.toTypedArray(),
            checkedEquipsReplicate,
            listener
        )
        builder.setPositiveButton("Replicar") { _, _ ->
            CoroutineScope(MainScope().coroutineContext).async {
                checkedEquipsReplicate.forEachIndexed { i, equip ->
                    if (equip) {
                        this@Order.equipment = arrayEquipsReplicate[i]
                        this@Order.id =
                            (Integer.parseInt(this@Order.id) + i + 1).toString() //Add 1 in ID
                        replication()
                    }
                }
            }
        }
        builder.setNegativeButton("Não Replicar") { _, _ -> }
        builder.show()
    }


    fun replication(){

        val textInputLayout = TextInputLayout(context)

        val editText = EditText(context)
        editText.text.insert(0,"PAT")
        editText.paddingLeft.plus(20)
        editText.paddingRight.plus(20)


        val inputBuilder = AlertDialog.Builder(context)
        inputBuilder.setTitle("Replicação")
        inputBuilder.setMessage("Insira a PAT para o equipamento $local - $equipment")
        inputBuilder.setView(editText)
        inputBuilder.setPositiveButton("Ok") {_,_ ->
            this@Order.pat = editText.text.toString()
            httpServices.addOrder(this@Order.serialize())
        }
        inputBuilder.show()
        editText.requestFocus()
    }

    fun serialize():OrderDTO{
        return OrderDTO(
            id,
            dateEnd,
            equipment,
            getImgKeysBefore(),
            getImgKeysAfter(),
            employeeId!!,
            gasKG,
            plant,
            local,
            obs,
            pat,
            getTypeServices(),
            getTypeManut(),
            getTypeSwap()
        )
    }


}