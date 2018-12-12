package com.kaori.kaori.BottomBarFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.R;

public class FilterFragment extends Fragment {
    /**
     * Constants
     */
    private final String BACK_STATE_NAME = getClass().getName();

    /**
     * Variables
     */
    private View view;
    private Button filterButton;
    private ChipGroup coursesGroup, examsGroup, professorsGroup;
    private FirebaseFirestore db;
    private String exam, professor, course;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.filter_layout, container, false);
        db = FirebaseFirestore.getInstance();

        setUpView();
        setUpButton();
        setUpData();

        return view;
    }

    private void setUpView(){
        filterButton = view.findViewById(R.id.filterButton);
        coursesGroup = view.findViewById(R.id.coursesChipGroup);
        coursesGroup.setSingleSelection(true);
        examsGroup = view.findViewById(R.id.examsChipGroup);
        examsGroup.setSingleSelection(true);
        professorsGroup = view.findViewById(R.id.professorChipGroup);
        professorsGroup.setSingleSelection(true);
    }

    private void setUpButton(){
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!exam.equals("") || !professor.equals("") || !course.equals("")) {
                    SearchFragment searchFragment = new SearchFragment();
                    searchFragment.filterBookQuery(exam, professor, course);
                }
            }
        });
    }

    private void setUpData(){
        db.collection("exams")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            examsGroup.addView(setChip(document.getString("name")));
                        }
                    }
                });

        db.collection("courses")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            coursesGroup.addView(setChip(document.getString("name")));
                        }
                    }
                });

        db.collection("professors")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            professorsGroup.addView(setChip(document.getString("name")));
                        }
                    }
                });
    }

    /**
     * This method invokes the book fragment when the card is clicked
     */
    private void invokeFragment(Fragment fragment) {
        if(getActivity()!= null && isAdded()) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }

    /**
     * This method is used to set up the chips
     */
    private Chip setChip(String text){
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setTextSize(R.dimen.default_chip_text_size);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setCloseIconVisible(false);
        chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    exam = chip.getText().toString();
            }
        });
        return chip;
    }

}
