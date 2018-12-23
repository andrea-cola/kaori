package com.kaori.kaori.FeedFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.List;
import static org.apache.commons.collections4.ListUtils.intersection;

public class FeedFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();

    private RecyclerView.Adapter mAdapter;
    private ArrayList<Material> mMaterialList;
    private User me;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.feed_layout, container, false);
        me = DataManager.getInstance().getUser();
        initializeView(view);
        downloadData();
        return view;
    }

    private void initializeView(View view){
        mMaterialList = new ArrayList<>();

        RecyclerView recyclerView = view.findViewById(R.id.my_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        if(DataManager.getInstance().getFeedElements().size() > 0)
            mAdapter = new RecyclerAdapter(DataManager.getInstance().getFeedElements());
        else
            mAdapter = new RecyclerAdapter(mMaterialList);
        recyclerView.setAdapter(mAdapter);

        if(DataManager.getInstance().getFeedElements().size() > 0)
            view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
    }

    private void downloadData(){
        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MATERIALS)
                .whereEqualTo(Constants.FIELD_COURSES, me.getCourse())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Material material = document.toObject(Material.class);
                            if (examsIntersection(material.getExams(), me.getExams()))
                                mMaterialList.add(material);
                        }
                        DataManager.getInstance().setFeedElements(mMaterialList);
                        mAdapter.notifyDataSetChanged();
                        view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                        if(mMaterialList.size() == 0)
                            view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                    }
                    else {
                        view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                        view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                        LogManager.getInstance().showVisualError(task.getException(), getString(R.string.generic_error));
                    }
                });
    }

    private boolean examsIntersection(List<String> exams1, List<String> exams2){
        return intersection(exams1, exams2).size() > 0;
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

        private final int LIBRO = 0;
        private final int FILE = 1;
        private final int URL = 2;

        List<Material> materials;

        /*package-private*/ RecyclerAdapter(List<Material> materials){
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
            holder.courseView.setText(materials.get(i).getCourse());

            if (getItemViewType(i) == LIBRO){
                holder.author.setText(materials.get(i).getProfessors().get(0));
                holder.image.setImageDrawable(getResources().getDrawable(R.drawable.book_icon));
            }else if (getItemViewType(i) == FILE){
                holder.author.setText(materials.get(i).getUser().getName());
                holder.image.setImageDrawable(getResources().getDrawable(R.drawable.document_icon));
            }else {
                holder.author.setText(materials.get(i).getUser().getName());
                holder.image.setImageDrawable(getResources().getDrawable(R.drawable.link_icon));
            }

            holder.cardView.setOnClickListener(v -> {
                MaterialFragment materialFragment = new MaterialFragment();
                materialFragment.setMaterial(materials.get(i));
                invokeNextFragment(materialFragment);
            });
        }

        @Override
        public int getItemViewType(int position) {
            switch (materials.get(position).getType()) {
                case Constants.LIBRO:
                    return LIBRO;
                case Constants.FILE:
                    return FILE;
                default:
                    return URL;
            }
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        /*package-private*/ class Holder extends RecyclerView.ViewHolder {
            TextView title, author, courseView;
            CardView cardView;
            ImageView image;

            Holder (View view) {
                super(view);
                title = view.findViewById(R.id.card_title);
                author = view.findViewById(R.id.card_author);
                courseView = view.findViewById(R.id.card_type);
                cardView = view.findViewById(R.id.card_view);
                image = (ImageView) view.findViewById(R.id.card_image);
            }
        }
    }
}