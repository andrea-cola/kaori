package com.kaori.kaori.Kaori.HomeFragments;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaori.kaori.Constants;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.LogManager;

import java.util.List;

public class StarredAdapter extends RecyclerView.Adapter<StarredAdapter.Holder> {

    private List<Document> materials;
    private Activity activity;

    public StarredAdapter(List<Document> docs, Activity activity){
        this.materials = docs;
        this.activity = activity;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        LogManager.getInstance().printConsoleMessage(materials.get(i).getTitle());
        holder.title.setText(materials.get(i).getTitle());
        holder.author.setText(materials.get(i).getUser().getName());

        String s = materials.get(i).getExams().get(0);
        for(int j = 1; j < materials.get(i).getExams().size(); j++)
            s = s + ", " + materials.get(i).getExams().get(j);
        holder.exams.setText(s);

        if(materials.get(i).getSubtype() == Constants.BOOK){
            holder.docIcon.setVisibility(View.GONE);
            holder.linkIcon.setVisibility(View.GONE);
            holder.bookIcon.setVisibility(View.VISIBLE);
        } else if(materials.get(i).getSubtype() == Constants.FILE){
            holder.bookIcon.setVisibility(View.GONE);
            holder.linkIcon.setVisibility(View.GONE);
            holder.docIcon.setVisibility(View.VISIBLE);
        } else {
            holder.docIcon.setVisibility(View.GONE);
            holder.bookIcon.setVisibility(View.GONE);
            holder.linkIcon.setVisibility(View.VISIBLE);
        }

        holder.view.setOnClickListener(view -> {
            Intent intent = new Intent(activity, MaterialActivity.class);
            intent.putExtra("document", materials.get(i));
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    /*package-private*/ class Holder extends RecyclerView.ViewHolder {
        private View view;
        private TextView title, author, exams;
        private ImageView docIcon, linkIcon, bookIcon;

        private Holder(View itemView) {
            super(itemView);
            view = itemView;
            title = view.findViewById(R.id.title);
            exams = view.findViewById(R.id.exams);
            author = view.findViewById(R.id.owner);
            docIcon = view.findViewById(R.id.label_doc);
            linkIcon = view.findViewById(R.id.label_url);
            bookIcon = view.findViewById(R.id.label_book);
        }
    }
}
