package com.kaori.kaori.MyMaterialFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;

import java.util.List;

public class MyBooksFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_layout, container, false);

        // view setup
        RecyclerView recyclerView = view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerAdapter(DataManager.getInstance().getFeedElements()));

        // load feed
        DataManager.getInstance().loadMyBooks(recyclerView, view);

        return view;
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        List<Document> materials;

        /*package-private*/ RecyclerAdapter(List<Document> materials){
            this.materials = materials;
        }

        @NonNull
        @Override
        public RecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new RecyclerAdapter.Holder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.Holder holder, int i) {
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

}
