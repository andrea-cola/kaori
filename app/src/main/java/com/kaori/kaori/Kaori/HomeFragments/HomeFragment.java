package com.kaori.kaori.Kaori.HomeFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaori.kaori.App;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;

import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_layout, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerAdapter(DataManager.getInstance().getFeedElements()));

        ((TextView)view.findViewById(R.id.empty_view_text)).setText(R.string.empty_text_feed);
        App.setEmptyView(view.findViewById(R.id.empty_view));
        DataManager.getInstance().downloadFeed(recyclerView);

        return view;
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        private final int MODIFIED = 4;

        private List<Document> materials;

        /*package-private*/ RecyclerAdapter(List<Document> materials){
            this.materials = materials;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case Constants.BOOK:
                    return new BookHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_book_card, parent, false));
                case Constants.FILE:
                    return new BookHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_doc_card, parent, false));
                case Constants.URL:
                    return new BookHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_url_card, parent, false));
                default:
                    return new UpdateHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card_update, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, int i) {
            holder.title.setText(materials.get(i).getTitle());
            holder.author.setText(materials.get(i).getUser().getName());
            holder.date.setText(Constants.dateFormat.format(new Date(materials.get(i).getTimestamp()*Constants.constantDate)));

            DataManager.getInstance().loadImageIntoView(materials.get(i).getUser().getThumbnail(), holder.authorIcon, getContext());

            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MaterialActivity.class);
                intent.putExtra("document", materials.get(i));
                startActivity(intent);
            });

            if(getItemViewType(i) != MODIFIED) {
                String exams = "";
                for (String ex : materials.get(i).getExams())
                    exams = exams + ex + ", ";
                holder.exams.setText(exams.substring(0, exams.length() - 2));

                if(getItemViewType(i) == Constants.BOOK) {
                    ((BookHolder) holder).price.setText("€ " + String.format("%.2f", materials.get(i).getPrice()));
                    DataManager.getInstance().loadImageIntoBackgroundView(App.getDrawableFromRes(R.drawable.background_book), holder.background, getContext());
                } else if(getItemViewType(i) == Constants.URL)
                    DataManager.getInstance().loadImageIntoBackgroundView(App.getDrawableFromRes(R.drawable.background_cloud), holder.background, getContext());
                else
                    DataManager.getInstance().loadImageIntoBackgroundView(App.getDrawableFromRes(R.drawable.background_file), holder.background, getContext());

            } else {
                String details = materials.get(i).getUser().getName().split(" ")[0];
                details = details + " ha aggiornato il suo " + Constants.translateTypeCode(materials.get(i).getSubtype()) + ":";
                ((UpdateHolder)holder).details.setText(details);
            }
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(materials.get(position).getModified())
                return MODIFIED;
            return materials.get(position).getSubtype();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView title, author, date, exams;
            MaterialCardView cardView;
            ImageView authorIcon, background;

            /*package-private*/ Holder (View view) {
                super(view);
                exams = view.findViewById(R.id.exams);
                title = view.findViewById(R.id.title);
                author = view.findViewById(R.id.user_and_text);
                date = view.findViewById(R.id.date);
                cardView = view.findViewById(R.id.card_view);
                authorIcon = view.findViewById(R.id.profile_image);
                background = view.findViewById(R.id.background);
            }
        }

        class BookHolder extends Holder {
            TextView price;

            /*package-private*/ BookHolder (View view) {
                super(view);
                price = view.findViewById(R.id.price);
            }
        }

        class UpdateHolder extends Holder {
            TextView details;

            /*package-private*/ UpdateHolder (View view) {
                super(view);
                details = view.findViewById(R.id.details);
            }
        }

    }
}