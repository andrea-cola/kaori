package com.kaori.kaori.ProfileFragments.UploadFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaori.kaori.R;

public class UploadUrlFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_link_layout, null);
        return view;
    }

    /**
     * This method create the new material with link type
     */
    /*private void createNewLink(){
        if (Patterns.WEB_URL.matcher(mLink.getText()).matches()){
            newMaterial.setTitle(String.valueOf(mTitle.getText()));
            newMaterial.setType(tag);
            newMaterial.setUrl(String.valueOf(mLink.getText()));
            newMaterial.setComment(String.valueOf(mComment.getText()));
            endProcess();
        } else {
            Toast.makeText(getContext(), "Url non valido", Toast.LENGTH_SHORT).show();
        }
    }*/

}
