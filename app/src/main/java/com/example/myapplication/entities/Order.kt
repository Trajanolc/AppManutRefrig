package com.example.myapplication.entities


data class Order(
    private val pat: String,
    private val local: String,
    private val plant: String,
    private val equipment: String,
    private val obs: String,
    private val listImg: ListImg
) {

    private val id: String = ((System.currentTimeMillis() - 1645473084517) / 1000).toString()
    private var typeManut: ArrayList<String> = ArrayList(0)
    private var typeServices: ArrayList<String> = ArrayList(0)
    private var typeSwap: ArrayList<String> = ArrayList(0)

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

    //BlankChecks
    fun equipamentBlank() : Boolean{
        return equipment.isBlank() || equipment.equals(" ")
    }

    fun blankManut(): Boolean {
        return typeManut.isEmpty()
    }

    fun blankServices(): Boolean {
        return typeSwap.isEmpty()
    }

    fun blankSwap(): Boolean {
        return typeSwap.isEmpty()
    }

    fun insert(){
        listImg.compress()
        listImg.sendS3bucket(equipment,local)

    }
}
