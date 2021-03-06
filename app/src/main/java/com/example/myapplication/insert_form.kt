package com.example.myapplication


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
import com.example.myapplication.entities.ListImg
import com.example.myapplication.databinding.InsertFormBinding
import com.example.myapplication.entities.ListEquip
import com.example.myapplication.entities.Order
import com.example.myapplication.enum.Period
import com.example.myapplication.services.DecimalDigitsInputFilter
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtInputGasKG.filters = arrayOf(DecimalDigitsInputFilter(2,3))

        val ListEquip = ListEquip(requireContext())
        ListEquip.resetPlant()

        binding.instalacao.adapter = ListEquip.arrayAdapterPlant
        binding.local.adapter = ListEquip.arrayAdapterLocal
        binding.equipamento.adapter = ListEquip.arrayAdapterEquip

        binding.instalacao.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                ListEquip.plantAtt(binding.instalacao.selectedItem.toString())

                //binding.local.adapter = ListEquip.arrayAdapterLocal
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                ListEquip.resetPlant()
            }
        }

        binding.local.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                ListEquip.localAtt(
                    binding.instalacao.selectedItem.toString(),
                    binding.local.selectedItem.toString()
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                ListEquip.resetPlant()
            }

        }


        binding.switchRecargaDeGas.setOnCheckedChangeListener { _, isChecked ->
            binding.recargaGasLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }



        binding.returnHome.setOnClickListener {
            findNavController().navigate(R.id.action_insert_form_to_FirstFragment)
        }


        val ListImg = ListImg(requireContext())
        binding.imgAntes.setOnClickListener {
            ListImg.addImgs(
                requireActivity().activityResultRegistry,
                Period.BEFORE,
                binding.imgAntes
            )

        }

        binding.imgDepois.setOnClickListener {
            ListImg.addImgs(
                requireActivity().activityResultRegistry,
                Period.AFTER,
                binding.imgDepois
            )
        }



        binding.finalizar.setOnClickListener {

            //Create order Object
            val order = Order(
                binding.chamado.text.toString(),
                binding.local.selectedItem.toString(),
                binding.instalacao.selectedItem.toString(),
                binding.equipamento.selectedItem.toString(),
                binding.OBS.text.toString(),
                ListImg,
                requireContext()
            )

            order.setTypeManut(
                binding.switchSensitiva.isChecked,
                binding.switchPreventiva.isChecked,
                binding.switchCorretiva.isChecked
            )

            order.setTypeServices(
                binding.switchRecargaDeGas.isChecked,
                binding.switchLimpezaFiltros.isChecked,
                binding.switchLimpezaQuimico.isChecked,
                binding.switchDreno.isChecked,
                binding.switchControle.isChecked,
                binding.switchRele.isChecked
            )

            order.setTypeSwap(
                binding.switchSensorTemperatura.isChecked,
                binding.switchSensorDegelo.isChecked,
                binding.switchPlaca.isChecked,
                binding.switchVentiladorEvap.isChecked,
                binding.switchVentiladorCond.isChecked,
                binding.switchSerpentina.isChecked,
                binding.switchCompressor.isChecked,
                binding.switchFusivel.isChecked,
                binding.switchCapacitor.isChecked,
                binding.switchRele.isChecked
            )

            order.setNumericInfo(
                binding.switchRecargaDeGas.isChecked,
                binding.txtInputGasKG.text.toString()
            )

            //Checks

            //check if equipament is blank
            if (order.equipamentBlank()) {
                binding.scroll.fullScroll(binding.scroll.top)
                Toast.makeText(
                    requireContext(),
                    "Por favor, preencha o equipamento.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            //check if at least one manut is selected
            if (order.blankManut()) {
                binding.scroll.fullScroll(binding.scroll.top)
                Toast.makeText(
                    requireContext(),
                    "Por favor, selecione ao menos uma manuten????o.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            //check if at least one service or swap is selected
            if (order.blankServices() && order.blankSwap()) {
                binding.scroll.fullScroll(binding.scroll.top)
                Toast.makeText(
                    requireContext(),
                    "Por favor, selecione ao menos um servi??o ou material.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            //Check if there is images
            if (ListImg.listsBlank()) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Sem imagens")
                builder.setMessage("N??o existem imagens ap??s o servi??o, deseja continuar?")
                builder.setPositiveButton("N??o") { _, _ ->
                    Toast.makeText(context, "Envio cancelado.", Toast.LENGTH_SHORT).show()
                }
                builder.setNegativeButton("Sim") { _, _ ->
                    order.insert(this)
                }
                builder.show()
            } else {
                order.insert(this)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}


