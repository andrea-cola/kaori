package com.kaori.kaori.MaterialFragments;

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
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;

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
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.filter_layout, container, false);
        db = FirebaseFirestore.getInstance();
        exam= "";
        professor = "";
        course = "";
        currentUser = DataManager.getInstance().getUser();

        setUpView();

        setUpButton();

        setUpExams();

        setUpCourses();

        setUpProfessors();

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
                MaterialFragment materialFragment = new MaterialFragment();
                if(!exam.equals("")){
                    materialFragment.filterBy("exam", exam);
                    invokeFragment(materialFragment);
                }else if(!professor.equals("") ){
                    materialFragment.filterBy("professor", professor);
                    invokeFragment(materialFragment);
                }else if(!course.equals("")){
                    invokeFragment(materialFragment);
                    materialFragment.filterBy("course", course);
                }else{
                    Toast.makeText(getContext(), "Seleziona un filtro", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpExams(){
        for(String e : currentUser.getExams()){
            Chip chip = setChip(new Chip(getContext()), e);
            examsGroup.addView(chip);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chip.isChecked())
                        exam = chip.getText().toString();
                }
            });
        }
    }

    private void setUpCourses(){
        db.collection("courses")
                .whereEqualTo("university", currentUser.getUniversity())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Chip chip = new Chip(getContext());
                            coursesGroup.addView(setChip(chip, document.getString("name")));
                            chip.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (chip.isChecked()) {
                                        course = chip.getText().toString();
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void setUpProfessors(){
        db.collection("professors")
                .whereEqualTo("university", currentUser.getUniversity())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Chip chip = new Chip(getContext());
                            professorsGroup.addView(setChip(chip, document.getString("name")));
                            chip.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (chip.isChecked())
                                        professor = chip.getText().toString();
                                }
                            });
                        }
                    }
                });
    }

    /**
     * This method invokes the book fragment when the card is clicked
     */
    private void invokeFragment(Fragment fragment) {
        if(getActivity()!= null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
        }
    }

    /**
     * This method is used to set up the chips
     */
    private Chip setChip(Chip chip, String text){
        chip.setText(text);
        chip.setTextSize(getResources().getDimension(R.dimen.default_chip_text_size));
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setCloseIconVisible(false);
        return chip;
    }
}
