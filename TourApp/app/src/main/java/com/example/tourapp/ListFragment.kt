package com.example.tourapp

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.AdapterContextMenuInfo
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tourapp.data.MyPlaces
import com.example.tourapp.databinding.FragmentListBinding
import com.example.tourapp.model.MyPlacesViewModel

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    // private lateinit var places:ArrayList<String>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val myPlacesViewModel: MyPlacesViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding.viewmyplaceNameText.text = myPlacesViewModel.selected?.name
        val myPlacesList: ListView = requireView().findViewById<ListView>(R.id.my_places_list)
        myPlacesList.adapter = ArrayAdapter<MyPlaces>(view.context, android.R.layout.simple_list_item_1, myPlacesViewModel.myPlacesList)

        myPlacesList.setOnItemClickListener(object: AdapterView.OnItemClickListener{
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long){
                var myPlaces: MyPlaces = p0?.adapter?.getItem(p2) as MyPlaces
                //Toast.makeText(view.context, myPlaces.description.toString(), Toast.LENGTH_SHORT).show()
                myPlacesViewModel.selected = myPlaces
                findNavController().navigate(R.id.action_ListFragment_to_ViewFragment)

                }
        })
        myPlacesList.setOnCreateContextMenuListener(object: View.OnCreateContextMenuListener{
            override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
                val info = menuInfo as AdapterContextMenuInfo
                val myPlaces: MyPlaces = myPlacesViewModel.myPlacesList[info.position]
                menu?.setHeaderTitle(myPlaces.name)
                menu?.add(0,1,1,"View place")
                menu?.add(0,2,2,"Edit place")
                menu?.add(0,3,3,"Delete place")
                menu?.add(0,4,4,"Show on map")
            }
        })
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        if (item.itemId === 1){
            myPlacesViewModel.selected = myPlacesViewModel.myPlacesList[info.position]
            this.findNavController().navigate(R.id.action_ListFragment_to_ViewFragment)
        } else if(item.itemId === 2)
        {
            myPlacesViewModel.selected = myPlacesViewModel.myPlacesList[info.position]
            this.findNavController().navigate(R.id.action_ListFragment_to_EditFragment)
        } else if (item.itemId ===3){
            myPlacesViewModel.myPlacesList.removeAt(info.position)
            val myPlacesList: ListView = requireView().findViewById<ListView>(R.id.my_places_list)
            myPlacesList.adapter = this@ListFragment.context?.let { ArrayAdapter<MyPlaces>(it, android.R.layout.simple_list_item_1,myPlacesViewModel.myPlacesList)}
        }else if(item.itemId === 4){
            myPlacesViewModel.selected = myPlacesViewModel.myPlacesList[info.position]
            this.findNavController().navigate(R.id.action_ListFragment_to_MapFragment)
        }
        return super.onContextItemSelected(item)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_new_place -> {
                this.findNavController().navigate(R.id.action_ListFragment_to_EditFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.action_my_places_list)
        item.isVisible = false;
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }
}