package com.kaori.kaori.ProfileFragments;

import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.LogManager;

public class UploadWaitFragment extends Fragment {

    /**
     * Constants.
     */
    private final String GS_URL = "gs://kaori-c5a43.appspot.com";

    /**
     * Variables.
     */
    private StorageReference storage;
    private String tag;
    private Material material;

    /**
     * Override of the method onCreateView.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wait_layout, container, false);
        LogManager.setView(view);
        storage = FirebaseStorage.getInstance().getReferenceFromUrl(GS_URL);

        if(tag.equalsIgnoreCase(Constants.FILE))
            uploadFileIntoStorage(material.getUrl());
        else if(tag.equalsIgnoreCase(Constants.URL) || tag.equalsIgnoreCase(Constants.LIBRO))
            writeDatabase(material);

        return view;
    }

    public void setParameters(String tag, Material material) {
        this.tag = tag;
        this.material = material;
    }

    private void writeDatabase(Material material) {
        material.setTimestamp(Timestamp.now());
        DocumentReference doc = FirebaseFirestore.getInstance().collection(Constants.DB_COLL_MATERIALS).document();
        material.setId(doc.getId());
        doc.set(material)
            .addOnSuccessListener(documentReference -> goBackToPreviousFragment())
            .addOnFailureListener(e -> LogManager.getInstance().printConsoleError("Error adding document: " + e.toString()));
    }

    private void uploadFileIntoStorage(String url) {
        StorageReference reference = storage.child(Constants.STORAGE_PATH_UPLOADS + DataManager.getInstance().getMiniUser().getName() + "_" + material.getTitle().toLowerCase() + ".pdf");
        UploadTask task = reference.putFile(Uri.parse(url));
        Log.d(Constants.TAG, "CIAONE " + url);

        task.addOnSuccessListener(taskSnapshot -> {
            material.setUrl(taskSnapshot.getUploadSessionUri().toString());
            Log.d(Constants.TAG, "CIAONE1 " + url);
            writeDatabase(material);
        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void goBackToPreviousFragment(){
        if(getActivity() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ProfileFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
    }

}