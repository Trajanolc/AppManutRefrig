package com.example.myapplication


import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.InsertFormBinding
import com.example.myapplication.entities.ListEquip
import com.example.myapplication.entities.ListImg
import com.example.myapplication.entities.Order
import com.example.myapplication.enum.Period
import com.example.myapplication.exceptions.HttpException
import com.example.myapplication.exceptions.OrderException
import com.example.myapplication.services.DecimalDigitsInputFilter


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

        binding.txtInputGasKG.filters = arrayOf(DecimalDigitsInputFilter(2, 3))

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
            try {
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
                order.checkImagesAndInsert(this)
            }
            catch (e: OrderException) {
                binding.scroll.fullScroll(binding.scroll.top)
                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            catch (e:HttpException){
                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
        }
    }

    fun goHome(){
        this.findNavController().navigate(R.id.action_insert_form_to_FirstFragment)
    }

    fun goUp(message:String){
        binding.scroll.fullScroll(binding.scroll.top)
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


