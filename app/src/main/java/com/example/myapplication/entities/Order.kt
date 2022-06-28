package com.example.myapplication.entities

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.services.DynamoAws
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


data class Order(
    val pat: String,
    val local: String,
    val plant: String,
    var equipment: String,
    val obs: String,
    val listImg: ListImg,
    val context: Context
) {

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
        return if (typeServices.isNotEmpty()) typeServices else arrayListOf(" ")
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
    fun setTypeManut(sensitiva: Boolean, preventiva: Boolean, corretiva: Boolean) {
        typeManut.clear()
        if (sensitiva) typeManut.add("Manutenção Sensitiva")
        if (preventiva) typeManut.add("Manutenção Preventiva")
        if (corretiva) typeManut.add("Manutenção Corretiva")
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
        rele: Boolean
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
        if (rele) typeSwap.add("Troca de Relé")
    }

    fun setNumericInfo(
        gas: Boolean,
        gasKGInput: String
    ) {
        if (gas) {
            gasKG = gasKGInput
        }
    }

    //BlankChecks
    fun equipamentBlank(): Boolean {
        return equipment.isBlank() || equipment == " "
    }

    fun blankManut(): Boolean {
        return typeManut.isEmpty()
    }

    fun blankServices(): Boolean {
        return typeSwap.isEmpty()
    }

    fun blankSwap(): Boolean {
        return typeServices.isEmpty()
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

    fun insert(fragment: Fragment) {
        listImg.compress()
        generateImgKeys()
        listImg.sendS3bucket(local, imgKeysBefore, imgKeysAfter)

        CoroutineScope(MainScope().coroutineContext).async {
            DynamoAws().putOrder(this@Order, context)
            replicateOrder()
        }


        fragment.findNavController().navigate(R.id.action_insert_form_to_FirstFragment)


    }

    private fun replicateOrder() {
        val arrayEquipsReplicate = ListEquip(context).getNearEquips(plant, local)
        arrayEquipsReplicate.remove(equipment)
        if (arrayEquipsReplicate.isEmpty()) return
        val checkedEquipsReplicate = BooleanArray(arrayEquipsReplicate.size)


        val builder = AlertDialog.Builder(context)
        val listener = DialogInterface.OnMultiChoiceClickListener { _, i, boolean ->
            checkedEquipsReplicate[i] = boolean
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

                        DynamoAws().putOrder(this@Order, context)
                    }
                }
            }
        }
        builder.setNegativeButton("Não Replicar") { _, _ -> }
        builder.show()


    }
}