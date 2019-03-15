package com.kaori.kaori.HomeFragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaori.kaori.Model.Document;

import java.util.List;

public class StarredAdapter extends RecyclerView.Adapter<StarredAdapter.Holder> {

    private List<Document> materials;
    private int type;

    public StarredAdapter(List<Document> docs, int type){
        this.materials = docs;
        this.type = type;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // TODO: qui cambiare la view a seconda del tipo
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        holder.text.setText(materials.get(i).getTitle());
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    /*package-private*/ class Holder extends RecyclerView.ViewHolder {
        TextView text;

        /*package-private*/ Holder (View view) {
            super(view);
            text = view.findViewById(android.R.id.text1);
        }
    }
}
