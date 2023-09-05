package com.example.tourapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.AdapterContextMenuInfo
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tourapp.R
import com.example.tourapp.data.MyPlaces
import com.example.tourapp.data.User
import com.example.tourapp.data.UserObject
import com.example.tourapp.databinding.FragmentListBinding
import com.example.tourapp.model.MyPlacesListAdapter
import com.example.tourapp.model.MyPlacesViewModel
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

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
    private val db = Firebase.firestore
    private var searchType: String = "name"
    private var userName: String = UserObject.username!!
    private lateinit var myUser: User
    var lastCheckedRadioButton: RadioButton? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getList()

       /** //binding.viewmyplaceNameText.text = myPlacesViewModel.selected?.name
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
       */
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
            R.id.menu_leaderboard -> {
                this.findNavController().navigate(R.id.action_ListFragment_to_UsersFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
      //  val item = menu.findItem(R.id.action_my_places_list)
     //   item.isVisible = false;
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

    override fun onResume() {
        super.onResume()
        val searchView: SearchView = binding.svTable
        searchType = "name"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (searchType.equals("grade") && query.toIntOrNull() == null)
                    Toast.makeText(requireContext(), "Unesite broj", Toast.LENGTH_SHORT).show()
                else
                    getAndShowFiltredList(searchType, query, 0)
                return true
            }


            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty())
                    if (searchType.equals("grade") && newText.toIntOrNull() == null) {

                        Toast.makeText(requireContext(), "Unesite broj", Toast.LENGTH_SHORT).show()
                    } else
                        getAndShowFiltredList(searchType, newText, 1)
                else
                    getList()
                return true
            }
        })
    }

    fun listCreating(result: QuerySnapshot): kotlin.collections.ArrayList<MyPlaces> {

        var list: kotlin.collections.ArrayList<MyPlaces> = ArrayList()
        for (document in result) {
            var data = document.data
            var grades = HashMap<String, Double>()
            if (data["grades"] != null) {
                for (g in data["grades"] as HashMap<String, Double>)
                    grades[g.key] = g.value
            }
            var comments = HashMap<String, String>()
            if (data["comments"] != null) {
                for (c in data["comments"] as HashMap<String, String>)
                    comments[c.key] = c.value
            }

            list.add(
                MyPlaces(
                    data["name"].toString(),
                    data["description"].toString(),
                    data["longitude"].toString(),
                    data["latitude"].toString(),
                    data["autor"].toString(),
                    grades,
                    comments,
                    data["url"].toString(),
                    data["category"].toString(),
                    document.id
                )
            )

        }
        return list
    }
    fun getList() {

        myPlacesViewModel.myPlacesList.clear()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    db.collection("places")

                        .get()
                        .await()
                }

                myPlacesViewModel.myPlacesList.addAll(listCreating(result))

                showList(requireView(), myPlacesViewModel.myPlacesList)
            } catch (e: Exception) {
                Log.w("TAGA", "Greska", e)
            }
        }


    }

    fun showList(view: View, arrayList: ArrayList<MyPlaces>) {

        val listView: ListView = requireView().findViewById(R.id.my_places_list)

        val arrayAdapter = MyPlacesListAdapter(
            view.context,
            arrayList
        )
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var myPlace: MyPlaces = p0?.adapter?.getItem(p2) as MyPlaces
                myPlacesViewModel.selected = myPlace
                findNavController().navigate(R.id.action_ListFragment_to_ViewFragment)

            }
        })


        listView.setOnItemLongClickListener { parent, view, position, id ->
            var myPlace: MyPlaces = parent?.adapter?.getItem(position) as MyPlaces
            myPlacesViewModel.selected = myPlace

            showPopupMenu(view, position)
            true
        }


    }

    fun getAndShowFiltredList(field: String, query: String, category: Int) {
        myPlacesViewModel.myPlacesList.clear()


        CoroutineScope(Dispatchers.Main).launch {
            try {
                var result: QuerySnapshot
                if (field.equals("grade")) {
                    result = withContext(Dispatchers.IO) {
                        db.collection("places")

                            .get()
                            .await()

                    }
                    for (document in result) {
                        var data = document.data
                        var grades = HashMap<String, Double>()
                        var sum: Double = 0.0
                        if (data["grades"] != null) {
                            for (g in data["grades"] as HashMap<String, Double>) {
                                grades[g.key] = g.value
                                sum += g.value
                            }
                            sum /= grades.size

                        }
                        var comments = HashMap<String, String>()
                        if (data["comments"] != null) {
                            for (c in data["comments"] as HashMap<String, String>)
                                comments[c.key] = c.value
                        }

                        var url: String =
                            "places/" + data["name"] + data["latitude"].toString() + data["longitude"].toString() + ".jpg"
                        if (sum >= query.toDouble())
                            myPlacesViewModel
                                .addPlace(
                                    MyPlaces(
                                        data["name"].toString(),
                                        data["description"].toString(),
                                        data["longitude"].toString(),
                                        data["latitude"].toString(),
                                        data["autor"].toString(),
                                        grades,
                                        comments,
                                        data["url"].toString(),
                                        data["category"].toString(),
                                        document.id

                                    )
                                )

                    }

                    showList(requireView(), myPlacesViewModel.myPlacesList)

                } else {
                        if (category == 0) {

                            result = withContext(Dispatchers.IO) {
                                db.collection("places")
                                    .whereEqualTo(field, query)

                                    .get()
                                    .await()

                            }

                        } else {

                            result = withContext(Dispatchers.IO) {
                                db.collection("places")
                                    .whereGreaterThanOrEqualTo(field, query)

                                    .get()
                                    .await()
                            }

                        }




                    myPlacesViewModel.myPlacesList.addAll(listCreating(result))
                    showList(requireView(), myPlacesViewModel.myPlacesList)
                }
            } catch (e: Exception) {
                Log.w("TAGA", "Greska", e)
            }
        }


    }

    private fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        if (!myPlacesViewModel.selected!!.autor.equals(userName)) {
            val menu = popupMenu.menu.findItem(R.id.editPlace)
            menu.isVisible = false

        }

        popupMenu.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.viewPlace -> {

                    findNavController().navigate(R.id.action_ListFragment_to_ViewFragment)
                    true
                }

                R.id.editPlace -> {

                    findNavController().navigate(R.id.action_ListFragment_to_EditFragment)

                    true
                }

                R.id.showOnMap -> {

                    this.findNavController().navigate(R.id.action_ListFragment_to_MapFragment)

                    true
                }
                R.id.rankPlace -> {

                    this.findNavController().navigate(R.id.action_ListFragment_to_RankFragment)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }
}