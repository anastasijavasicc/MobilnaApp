package com.example.tourapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.example.tourapp.databinding.FragmentViewBinding
import com.example.tourapp.model.MyPlacesViewModel
import java.util.ArrayList


class ViewFragment : Fragment() {

    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()
    private var _binding: FragmentViewBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentViewBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmyplaceNameText.text = myPlacesViewModel.selected?.name
        binding.viewmyplaceDescText.text = myPlacesViewModel.selected?.description
        binding.viewmyplaceAutorText.text = myPlacesViewModel.selected?.autor
        binding.viewmyplaceLongText.text = myPlacesViewModel.selected?.longitude
        binding.viewmyplaceLatText.text = myPlacesViewModel.selected?.latitude
        binding.viewmyplaceCategoryText.text = myPlacesViewModel.selected?.category
        //myPlacesViewModel.selected = null

        var sum:Double = 0.0
        for (el in myPlacesViewModel.selected?.grades!!)
            sum += el.value
        if (myPlacesViewModel.selected?.grades!!.size!= 0)
            sum /= myPlacesViewModel.selected?.grades!!.size
        binding.ratingBar2.rating = sum.toFloat()

        var s: ArrayList<String> = ArrayList()
        for (el in myPlacesViewModel.selected?.comments!!)
            s.add(el.value)
        binding.viewFragmentListView.adapter=
            ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1,s)


        binding.ViewFragmentClose.setOnClickListener{
            myPlacesViewModel.selected = null
            findNavController().navigate(R.id.action_ViewFragment_to_ListFragment)
        }
        binding.btnOceni.setOnClickListener{
            findNavController().navigate(R.id.action_ViewFragment_to_RankFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        myPlacesViewModel.selected = null
    }

}