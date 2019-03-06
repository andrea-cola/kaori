package com.kaori.kaori.ProfileFragments.UploadFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.kaori.kaori.Model.Book;
import com.kaori.kaori.ProfileFragments.MyFilesFragment;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used to upload new books.
 */
public class UploadBookFragment extends Fragment {

    private View view;
    private TextView exams;
    private ArrayList<String> examsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upload_libro_layout, null);
        view.findViewById(R.id.button).setOnClickListener(v -> createNewBook());
        view.findViewById(R.id.button_exams).setOnClickListener(v -> selectExams());
        exams = view.findViewById(R.id.exams);

        examsList = new ArrayList<>();

        return view;
    }

    /**
     * Create the popup to choose the exams.
     */
    private void selectExams(){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_exams, null);
        LinearLayout checkboxSpace = dialogView.findViewById(R.id.checkboxSpace);
        List<CheckBox> checkBoxes = new ArrayList<>();

        for(int i = 0; i < DataManager.getInstance().getAllExams().size(); i++){
            CheckBox c = new CheckBox(getContext());
            c.setText(DataManager.getInstance().getAllExams().get(i));
            checkBoxes.add(c);
            checkboxSpace.addView(checkBoxes.get(checkBoxes.size()-1));
        }

        builder.setView(dialogView);
        dialog = builder.create();

        dialogView.findViewById(R.id.button).setOnClickListener(v -> {
            exams.setText("Nessun esame selezionato");
            examsList.clear();
            boolean flag = true;
            for(int i = 0; i < checkBoxes.size(); i++){
                if(checkBoxes.get(i).isChecked()) {
                    if(flag)
                        exams.setText(DataManager.getInstance().getAllExams().get(i));
                    else {
                        flag = false;
                        exams.setText(exams.getText() + ", " + DataManager.getInstance().getAllExams().get(i));
                    }
                    examsList.add(DataManager.getInstance().getAllExams().get(i));
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * This method create the new material with book type
     */
    private void createNewBook() {
        TextView title, author, editor, price, note;
        title = view.findViewById(R.id.title);
        author = view.findViewById(R.id.author);
        editor = view.findViewById(R.id.editor);
        price = view.findViewById(R.id.price);
        note = view.findViewById(R.id.note);

        Book book = new Book();
        book.setTimestamp(Timestamp.now());
        book.setTitle(String.valueOf(title.getText()));
        book.setAuthor(String.valueOf(author.getText()));
        book.setEditor(String.valueOf(editor.getText()));
        book.setPrice(Float.parseFloat(price.getText().toString()));
        book.setNote(String.valueOf(note.getText()));
        book.setUser(DataManager.getInstance().getMiniUser());
        book.setCourses(examsList);

        DataManager.getInstance().uploadBook(book); // upload on server

        endProcess();

        // TODO: aggiungere immagine di copertina
    }

    /**
     * Return to profile
     */
    private void endProcess() {
        if(getActivity() != null && getActivity().getSupportFragmentManager() != null) {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new MyFilesFragment())
                    .commit();
        }
    }

}
