package com.kaori.kaori.HomeFragments;

import android.app.FragmentTransaction;
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

import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;

import java.util.List;

/**
 * Feed fragment: first option in the bottom bar menu.
 */
public class HomeFragment extends Fragment {

    /**
     * Constants.
     */
    private final String BACK_STATE_NAME = getClass().getName();

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
        DataManager.getInstance().loadFeed(recyclerView, view);

        return view;
    }

    private void invokeNextFragment(Fragment fragment) {
        if(getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        List<Document> materials;

        /*package-private*/ RecyclerAdapter(List<Document> materials){
            this.materials = materials;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, int i) {
            holder.title.setText(materials.get(i).getTitle());
            holder.author.setText(materials.get(i).getUser().getName());
            holder.status.setText((materials.get(i).getModified() ? "Aggiornamento" : "Nuovo ").toUpperCase());
            holder.date.setText(Constants.dateFormat2.format(materials.get(i).getTimestamp().toDate()));

            String info = "";
            for(String ex : materials.get(i).getExams())
                info = info + ex + ", ";
            holder.info.setText(info.substring(0, info.length()-2));

            DataManager.getInstance().loadImageIntoView(materials.get(i).getUser().getThumbnail(), holder.authorIcon, getContext());

            holder.cardView.setOnClickListener(v -> {
                MaterialFragment materialFragment = new MaterialFragment();
                materialFragment.setMaterial(materials.get(i));
                invokeNextFragment(materialFragment);
            });
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        /*package-private*/ class Holder extends RecyclerView.ViewHolder {
            TextView title, status, info, author, comments, date, type;
            MaterialCardView cardView;
            ImageView authorIcon, materialIcon;

            /*package-private*/ Holder (View view) {
                super(view);
                title = view.findViewById(R.id.title);
                author = view.findViewById(R.id.author);
                info = view.findViewById(R.id.info);
                status = view.findViewById(R.id.course);
                date = view.findViewById(R.id.date);
                cardView = view.findViewById(R.id.card_view);
                authorIcon = view.findViewById(R.id.authorImage);
                comments = view.findViewById(R.id.comments);
                type = view.findViewById(R.id.type);
                materialIcon = view.findViewById(R.id.type_icon);
            }
        }
    }
}