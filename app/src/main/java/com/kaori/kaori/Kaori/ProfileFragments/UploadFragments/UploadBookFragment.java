package com.kaori.kaori.Kaori.ProfileFragments.UploadFragments;

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
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.Kaori.ProfileFragments.MyFilesFragment;
import com.kaori.kaori.R;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.LogManager;

import java.util.ArrayList;
import java.util.List;

public class UploadBookFragment extends Fragment {

    private TextView exams, title, author, editor, price, note;
    private List<String> examsList;
    private Document oldBook;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_libro_layout, null);
        view.findViewById(R.id.button).setOnClickListener(v -> createNewBook());
        view.findViewById(R.id.button_exams).setOnClickListener(v -> selectExams());
        exams = view.findViewById(R.id.exams);

        title = view.findViewById(R.id.title);
        author = view.findViewById(R.id.author);
        editor = view.findViewById(R.id.editor);
        price = view.findViewById(R.id.price);
        note = view.findViewById(R.id.status);

        examsList = new ArrayList<>();

        if(oldBook != null)
            preset();

        return view;
    }

    public void setOldBook(Document document){
        this.oldBook = document;
    }

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
                    if(flag) {
                        flag = false;
                        exams.setText(DataManager.getInstance().getAllExams().get(i));
                    }
                    else
                        exams.setText(exams.getText() + ", " + DataManager.getInstance().getAllExams().get(i));
                    examsList.add(DataManager.getInstance().getAllExams().get(i));
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void preset(){
        title.setText(oldBook.getTitle());
        author.setText(oldBook.getAuthor());
        editor.setText(oldBook.getEditor());
        price.setText(Float.toString(oldBook.getPrice()));
        note.setText(oldBook.getNote());

        String tmp = oldBook.getExams().get(0);
        for(int i = 1; i < oldBook.getExams().size(); i++)
            tmp = tmp + ", " + oldBook.getExams().get(i);
        exams.setText(tmp);
        examsList = oldBook.getExams();
    }

    private boolean checkBookParameters(){
        return title.getText().length()>0 && author.getText().length()>0 && editor.getText().length()>0 &&
                price.getText().length()>0 && note.getText().length()>0;
    }

    private void createNewBook() {
        Document book;
        if (oldBook != null)
            book = oldBook;
        else
            book = new Document();
        if (!checkBookParameters())
            LogManager.getInstance().showVisualMessage(getString(R.string.error_upload_msg));
        else {
            book.setTimestamp(Timestamp.now().getSeconds());
            book.setTitle(String.valueOf(title.getText()));
            book.setAuthor(String.valueOf(author.getText()));
            book.setEditor(String.valueOf(editor.getText()));
            book.setPrice(Float.parseFloat(price.getText().toString()));
            book.setNote(String.valueOf(note.getText()));
            book.setUser(DataManager.getInstance().getMiniUser());
            book.setExams(examsList);
            book.setCourse(DataManager.getInstance().getUser().getCourse());
            book.setModified(false);
            book.setUniversity(DataManager.getInstance().getUser().getUniversity());
            book.setType(Constants.BOOK);
            book.setSubtype(Constants.BOOK);

            DataManager.getInstance().uploadDocument(book); // upload on server
            endProcess();
        }
    }

    private void endProcess() {
        if(getActivity() != null && getActivity().getSupportFragmentManager() != null) {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new MyFilesFragment())
                    .commit();
        }
    }

}
