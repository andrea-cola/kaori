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
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();

    private RecyclerView.Adapter mAdapter;
    private List<Material> mMaterialList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_layout, container, false);

        // setting the parameters
        mMaterialList = new ArrayList<>();

        initializeView(view);

        downloadData();

        return view;
    }

    private void initializeView(View view){
        RecyclerView recyclerView = view.findViewById(R.id.my_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerAdapter(mMaterialList);
        recyclerView.setAdapter(mAdapter);
    }

    private void downloadData(){
        List<String> myExams = DataManager.getInstance().getUser().getExams();

        FirebaseFirestore.getInstance()
                .collection(Constants.DB_COLL_MATERIALS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            LogManager.getInstance().printConsoleMessage("eccoci3");
                            if (myExams.contains(String.valueOf(document.get(Constants.FIELD_EXAM)))) {
                                mMaterialList.add(document.toObject(Material.class));
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    else
                        LogManager.getInstance().printConsoleError("Error getting documents: " + task.getException());
                });
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
            //holder.author.setText(materials.get(i).getType().equals(Constants.LIBRO) ? materials.get(i).getProfessor() : materials.get(i).getUser().getName());
            holder.title.setText(materials.get(i).getTitle());
            holder.courseView.setText(materials.get(i).getCourse());

            holder.cardView.setOnClickListener(v -> {
                MaterialFragment bookFragment = new MaterialFragment();
                bookFragment.setParameters(holder.author.getText().toString(), holder.title.getText().toString());
                invokeNextFragment(bookFragment);
            });
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        /*package-private*/ class Holder extends RecyclerView.ViewHolder {
            TextView title, author, courseView;
            CardView cardView;
            //public ImageView image;

            Holder (View view) {
                super(view);
                title = view.findViewById(R.id.card_title);
                author = view.findViewById(R.id.card_author);
                courseView = view.findViewById(R.id.card_type);
                cardView = view.findViewById(R.id.card_view);
                //image = (ImageView) view.findViewById(R.id.thumbnail);
            }
        }
    }
}