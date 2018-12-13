package com.kaori.kaori.BottomBarFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.kaori.kaori.DBObjects.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    /**
     * Variables.
     */
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<Material> mMaterialList;
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * View elements
     */
    private FloatingActionButton fab;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.feed_layout, container, false);

        // setting the parameters
        mMaterialList = new ArrayList<>();

        setUpView();

        setUpButtons();

        setUpFirebase();

        return view;
    }

    /**
     * This method sets up the view elements
     */
    private void setUpView(){
        recyclerView = view.findViewById(R.id.my_recycler_view);
        fab = view.findViewById(R.id.feedFAB);

        mAdapter = new RecyclerAdapter(mMaterialList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * This methods sets up the listeners on the buttons of the view
     */
    private void setUpButtons(){
        // Floating Action Button management for upload pdf files
        fab.setOnClickListener(v -> {
            UploadBookFragment uploadFragment = new UploadBookFragment();
            invokeNextFragment(uploadFragment);
        });
    }

    /**
     * This method sets up the Firebase database
     */
    private void setUpFirebase(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("materials")
                .orderBy("timestamp")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (DataManager.getInstance().getUser().getExams().contains(String.valueOf(document.get("exam"))))
                                mMaterialList.add(document.toObject(Material.class));
                        }
                        if(getContext() != null)
                            mAdapter.notifyDataSetChanged();
                    } else {
                        LogManager.getInstance().printConsoleError("Error getting documents: " + task.getException());
                    }
                });
    }

    /**
     * This method invokes the book fragment when the card is cliked
     */
    private void invokeNextFragment(Fragment fragment) {
        if(getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }

    /**
     * Private Adapter for RecyclerView
     */
    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        List<Material> materials;

        /*package-private*/ RecyclerAdapter(List<Material> materials){
            this.materials = materials;
        }

        /**
         * This method inflates the CardView in the RecyclerView
         */
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, int i) {
            holder.author.setText(materials.get(i).getAuthor());
            holder.title.setText(materials.get(i).getTitle());
            holder.courseView.setText(materials.get(i).getCourse());

            holder.cardView.setOnClickListener(v -> {
                BookFragment bookFragment = new BookFragment();
                bookFragment.setParameters(holder.author.getText().toString(), holder.title.getText().toString());
                invokeNextFragment(bookFragment);
            });
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        /**
         * Holder class contains the info of the card and the book element selected or
         * the next book element in the recycler view
         */
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