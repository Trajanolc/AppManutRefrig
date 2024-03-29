package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.HomeBinding
import com.example.myapplication.adpters.HomeListAdapter
import com.example.myapplication.entities.SimplifiedOrderDTO
import com.example.myapplication.services.httpServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class Home : Fragment() {

    private var _binding: HomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = HomeBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val login = "Mario Marques"

        binding.addOS.setOnClickListener {

            findNavController().navigate(R.id.action_FirstFragment_to_insert_form)

                Toast.makeText(
                    requireContext(),
                    "Carregando formulário",
                    Toast.LENGTH_SHORT

                ).show()

        }
        val swipe = binding.swipeRefresh
        swipe.setOnRefreshListener {
            updateOrders(login)
            swipe.isRefreshing = false

        }


        updateOrders(login)


    }


    private fun updateOrders(login: String?) {
        val recycerview = binding.recyclerView
        recycerview.visibility = View.GONE
        val orderList = arrayListOf<SimplifiedOrderDTO>()
        var oAdapter = HomeListAdapter(orderList)
        recycerview.adapter = oAdapter
        CoroutineScope(MainScope().coroutineContext).async {
            if (login != "") {
                try{
                    val response = httpServices.getEmployeeOrder(login!!)
                    orderList.clear()
                    orderList.addAll(response.first)
                    binding.ordersCount.text="Ordens realizadas esse mês: ${response.second}"



                }catch (e : Exception){
                    orderList.clear()
                }finally {
                    binding.ordersCount.visibility = View.VISIBLE
                    binding.addOS.visibility = View.VISIBLE
                    binding.progress.visibility = View.GONE
                }


                if (orderList.isNotEmpty()) {
                    oAdapter.notifyItemMoved(0, orderList.size - 1)
                    recycerview.visibility = View.VISIBLE
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}