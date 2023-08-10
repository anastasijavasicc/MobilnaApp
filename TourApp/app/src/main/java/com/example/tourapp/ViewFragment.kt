package com.example.tourapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tourapp.databinding.FragmentViewBinding
import com.example.tourapp.model.MyPlacesViewModel


class ViewFragment : Fragment() {

    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()
    private var _binding: FragmentViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

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
        binding.viewmyplaceLongText.text = myPlacesViewModel.selected?.longitude
        binding.viewmyplaceLatText.text = myPlacesViewModel.selected?.latitude
        //myPlacesViewModel.selected = null
        binding.viewmyplaceFinishedButton.setOnClickListener{
            myPlacesViewModel.selected = null
            findNavController().navigate(R.id.action_ViewFragment_to_ListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        myPlacesViewModel.selected = null
    }

}