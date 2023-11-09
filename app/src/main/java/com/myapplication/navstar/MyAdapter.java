package com.myapplication.navstar;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    Context context;
    List<com.myapplication.navstar.List_Detail> items;
    com.myapplication.navstar.DatabaseSupport databaseSupport;
    boolean isDeleteButton = false;

    public MyAdapter(Context context, List<com.myapplication.navstar.List_Detail> items) {
        this.context = context;
        this.items = items;
        this.databaseSupport = databaseSupport;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.nameView.setText(items.get(position).getName());
        holder.addressView.setText(items.get(position).getAddress());
        holder.dateView.setText(items.get(position).getDate());

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SavedPlaceDetail.class);
            intent.putExtra("Name", items.get(holder.getAdapterPosition()).getName());
            intent.putExtra("Address", items.get(holder.getAdapterPosition()).getAddress());
            intent.putExtra("placeId", items.get(holder.getAdapterPosition()).getPlaceId());
            //intent.putExtra("Date", items.get(holder.getAdapterPosition()).getDate());
            context.startActivity(intent);
        });

        if(isDeleteButton){
            holder.deleteButton.setVisibility(View.VISIBLE);
        }else{
            holder.deleteButton.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(del -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position);
            }
        });
    }

    public void toggleImageVisibility() {
        isDeleteButton = !isDeleteButton;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private OnDeleteClickListener deleteClickListener;

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

}

class MyViewHolder extends RecyclerView.ViewHolder{

    TextView nameView, addressView, dateView;
    CardView cardView;
    ImageButton deleteButton;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        nameView = itemView.findViewById(R.id.name_listItems);
        addressView = itemView.findViewById(R.id.address_listItems);
        dateView = itemView.findViewById(R.id.date_listItems);
        cardView = itemView.findViewById(R.id.item_listItems);
        deleteButton = itemView.findViewById(R.id.deleteButton_listItems);
    }
}

