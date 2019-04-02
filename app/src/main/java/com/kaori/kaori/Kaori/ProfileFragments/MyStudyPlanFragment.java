package com.kaori.kaori.Kaori.ProfileFragments;

import android.app.FragmentTransaction;
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

import com.kaori.kaori.App;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;

import java.util.List;

public class MyStudyPlanFragment extends Fragment {

    private final String BACK_STATE_NAME = getClass().getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_exams, container, false);

        if(DataManager.getInstance().getUser().getExams().size() > 0) {
            RecyclerView recyclerView = view.findViewById(R.id.exam_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new RecyclerAdapter(DataManager.getInstance().getUser().getExams()));
        } else {
            ((TextView)view.findViewById(R.id.empty_view_text)).setText(R.string.empty_text_feed);
            App.setEmptyView(view.findViewById(R.id.empty_view));
            App.setAuxiliarViewsStatus(Constants.EMPTY_VIEW_ACTIVE);
        }

        view.findViewById(R.id.modify_exams).setOnClickListener(v -> invokeNextFragment(new EditPlanFragment()));
        return view;
    }

    private void invokeNextFragment(Fragment fragment) {
        if(getActivity() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder>{

        private List<String> exams;
        private int counter;

        /*package-private*/ RecyclerAdapter(List<String> exams){
            this.exams = exams;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_exam_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            holder.examTitle.setText(exams.get(i));
            counter = 0;
            for (Document doc : DataManager.getInstance().getMyFiles())
                if (doc.getExams().contains(exams.get(i)))
                    counter++;
            holder.examCounter.setText(counter + " documenti collegati");
        }

        @Override
        public int getItemCount() {
            return exams.size();
        }

        /*package-private*/ class Holder extends RecyclerView.ViewHolder {
            private TextView examTitle, examCounter;
            private View view;

            /*package-private*/ Holder(View itemView) {
                super(itemView);
                view = itemView;
                examTitle = itemView.findViewById(R.id.exam_title);
                examCounter = itemView.findViewById(R.id.doc_counter);
            }
        }

    }
}
