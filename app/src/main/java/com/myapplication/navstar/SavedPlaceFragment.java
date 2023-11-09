package com.myapplication.navstar;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class SavedPlaceFragment extends Fragment implements com.myapplication.navstar.MyAdapter.OnDeleteClickListener{
    private com.myapplication.navstar.DatabaseSupport databaseSupport;
    private com.myapplication.navstar.MyAdapter adapter;
    private Context savedContext;
    private List<com.myapplication.navstar.List_Detail> items = new ArrayList<>();
    private boolean isInitialized = false;

    public SavedPlaceFragment(Context context) {
        savedContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        View rootView = inflater.inflate(R.layout.fragment_saved_place, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerviewItemSaved);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        databaseSupport = ((com.myapplication.navstar.MainActivity) savedContext).getDatabaseSupport();

        com.myapplication.navstar.DatabaseSupport databaseSupport1 = com.myapplication.navstar.DatabaseSupportSingleton.getInstance(savedContext);

        if(items.isEmpty()) {
            itemsSaved();
        }
        adapter = new com.myapplication.navstar.MyAdapter(requireContext(), items);
        adapter.setOnDeleteClickListener((com.myapplication.navstar.MyAdapter.OnDeleteClickListener) this);
        recyclerView.setAdapter(adapter);

        isInitialized = true;
        return rootView;
    }

    @Override
    public void onDeleteClick(int position) {
        String name = items.get(position).getName();
        com.myapplication.navstar.List_Detail detail = new com.myapplication.navstar.List_Detail(name);
        databaseSupport.deletePlace(detail);
        items.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, items.size());
        Snackbar.make(getView(), "Luogo: " + detail.getName() + " è stato eliminato.",  Snackbar.LENGTH_SHORT).show();
    }

    public void addItems(com.myapplication.navstar.List_Detail newItems) {
        int startPosition = items.size();
        items.add(newItems);
        adapter.notifyItemRangeInserted(startPosition, items.size()+1);
    }

    public void itemsSaved(){
        List<com.myapplication.navstar.List_Detail> details = databaseSupport.getAll();
        for (com.myapplication.navstar.List_Detail rowList : details) {
            String placeName = rowList.getName();
            String placeAddress = rowList.getAddress();
            String placeId = rowList.getPlaceId();
            String placeDate = rowList.getDate();
            Log.i("TAG", "MESSAGGIO DI PROVA: " + placeName);
            items.add(new com.myapplication.navstar.List_Detail(placeName, placeAddress, placeId, placeDate));
        }
    }

    public Boolean getInitialized(){
        return isInitialized;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu2, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.toolbar_action_settings){
            AlertDialog.Builder builder = new AlertDialog.Builder(savedContext);
            builder.setMessage("Navstar! \n" + "La tua app per gestire  una lista di segnaposti geografici.");
            builder.show();
        }else if(id == R.id.toolbar_delete){
            adapter.toggleImageVisibility();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}