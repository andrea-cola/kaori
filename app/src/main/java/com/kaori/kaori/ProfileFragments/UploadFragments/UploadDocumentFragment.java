package com.kaori.kaori.ProfileFragments.UploadFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.DataManager;

public class UploadDocumentFragment extends Fragment {

    private Document newMaterial;
    private boolean isModified;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_file_layout, null);

        isModified = false;

        return view;
    }

    private void initializeNewMaterial(){
        newMaterial = new Document();
        newMaterial.setUser(DataManager.getInstance().getMiniUser());
        newMaterial.setCourse(DataManager.getInstance().getUser().getCourse());
        newMaterial.setModified(isModified);
    }

    /**
     * This method create the new material with file type
     */
    /*private void createNewFile(String url) {
        StorageReference reference = storage
                .child(Constants.STORAGE_PATH_UPLOADS + DataManager.getInstance().getMiniUser().getName() + "_" + String.valueOf(mTitle.getText()) + ".pdf");
        if(isMaterialModified)
            reference.delete()
                    .addOnFailureListener(e -> LogManager.getInstance().printConsoleError("Error creating new document: " + e.toString()));
        newMaterial.setTitle(String.valueOf(mTitle.getText()));
        newMaterial.setNote(String.valueOf(mComment.getText()));
        newMaterial.setType(tag);
        newMaterial.setUrl(url);
        endProcess();
    }

    /**
     * This method start the activity to get the pdf file
     */
    /*private void getPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_PDF_CODE);
    }

    /**
     * This method is called when the intent is returned for getting the pdf file
     */
    /*public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null)
            if (data.getData() != null) {
                createNewFile(data.getData().toString());
            }
            else
                Toast.makeText(getContext(), "No file chosen", Toast.LENGTH_SHORT).show();
    }*/

}
