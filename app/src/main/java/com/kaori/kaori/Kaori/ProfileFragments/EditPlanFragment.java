package com.kaori.kaori.Kaori.ProfileFragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.kaori.kaori.App;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

import java.util.ArrayList;
import java.util.List;

public class EditPlanFragment extends Fragment {

    private List<String> selectedCourses;
    private List<String> allExams;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_exams, container, false);
        setHasOptionsMenu(true);
        view.findViewById(R.id.button).setOnClickListener(v -> saveData());
        view.findViewById(R.id.addExamButton).setOnClickListener(v -> showPopup());

        selectedCourses = DataManager.getInstance().getUser().getExams();
        allExams = DataManager.getInstance().getAllExams();

        recyclerView = view.findViewById(R.id.examList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerAdapter(selectedCourses));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.empty_menu, menu);
    }

    private void showPopup(){
        AlertDialog alertDialog;
        List<String> list = getAvailableExams();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_plan_popup, null);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, list);
        Spinner sp = dialogView.findViewById(R.id.exams);
        adapter.setNotifyOnChange(true);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        alertDialog = builder.create();
        alertDialog.show();

        dialogView.findViewById(R.id.confirm).setOnClickListener(v -> {
            String ex = list.get(sp.getSelectedItemPosition());
            selectedCourses.add(ex);
            recyclerView.getAdapter().notifyDataSetChanged();
            alertDialog.dismiss();
        });
    }

    private List<String> getAvailableExams(){
        List<String> exams = new ArrayList<>();
        for(String exam : allExams)
            if(!selectedCourses.contains(exam))
                exams.add(exam);
        return exams;
    }

    /**
     * Save data into database.
     */
    private void saveData() {
        User user = DataManager.getInstance().getUser();
        user.setExams(selectedCourses);
        DataManager.getInstance().updateUser(null);
        DataManager.getInstance().downloadAllExams();
        endProcess(true);
    }

    private void removeExams(int i){
        selectedCourses.remove(i);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * This method is called whenever the process is terminated.
     * Return to the previous fragment.
     */
    private void endProcess(boolean isSuccess){
        App.setAuxiliarViewsStatus(Constants.NO_VIEW_ACTIVE);
        if (isSuccess && getActivity() != null) {
            LogManager.getInstance().printConsoleMessage("endProcess:success");
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        }
        else if(!isSuccess && getActivity() != null) {
            (new Handler()).postDelayed(() -> getActivity().getSupportFragmentManager().popBackStackImmediate(), 3000);
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder>{

        private List<String> exams;

        /*package-private*/ RecyclerAdapter(List<String> exams){
            this.exams = exams;
        }

        @NonNull
        @Override
        public RecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new RecyclerAdapter.Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_exam_item_editable, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerAdapter.Holder holder, int i) {
            holder.examTitle.setText(exams.get(i));
            holder.delete.setOnClickListener(v -> removeExams(i));
        }

        @Override
        public int getItemCount() {
            return exams.size();
        }

        /*package-private*/ class Holder extends RecyclerView.ViewHolder {
            private TextView examTitle;
            private ImageButton delete;

            /*package-private*/ Holder(View v) {
                super(v);
                examTitle = v.findViewById(R.id.exam_title);
                delete = v.findViewById(R.id.delete);
            }
        }

    }

}
