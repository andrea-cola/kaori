package com.kaori.kaori.FeedFragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.BuildConfig;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.FileManager;

import java.io.File;

public class MaterialFragment extends Fragment {

    private View view;
    private StorageReference storage;
    private Material mMaterial;

    public MaterialFragment(){}

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.material_layout, container, false);
        storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://kaori-c5a43.appspot.com");

        initializeView();

        return view;
    }


    public void setMaterial(Material material){
        this.mMaterial = material;
    }

    private void initializeView(){
        LinearLayout linearLayout = view.findViewById(R.id.linearLayout);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mMaterial.getType().equalsIgnoreCase(Constants.LIBRO)) {
            linearLayout.addView(inflater.inflate(R.layout.libro_layout, null));
            setLibroLayout();
        }else if (mMaterial.getType().equalsIgnoreCase(Constants.FILE)) {
            linearLayout.addView(inflater.inflate(R.layout.file_layout, null));
            setFileLayout();
        }else {
            linearLayout.addView(inflater.inflate(R.layout.link_layout, null));
            setLinkLayout();
        }
        initializeSubView();

    }


    private void setLibroLayout(){
        TextView author = view.findViewById(R.id.author);
        author.setText(mMaterial.getProfessors().get(0));

    }

    private void setFileLayout(){
        Button downloadButton = view.findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(v -> storage.child(Constants.STORAGE_PATH_UPLOADS + mMaterial.getUser().getName() + "_" + mMaterial.getTitle() + ".pdf").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                File pdfFile = new File(Constants.STORAGE_PATH + mMaterial.getTitle() + ".pdf");
                if (!pdfFile.exists()){
                    FileManager fileManager = new FileManager(mMaterial.getTitle(), uri, getActivity(), getContext());
                    if(fileManager.download())
                        show();
                } else
                    show();
            }
        }));
    }

    private void setLinkLayout(){
        TextView author = view.findViewById(R.id.author);
        author.setText(mMaterial.getUser().getName());
        TextView link = view.findViewById(R.id.url);
        link.setText(mMaterial.getUrl());
        link.setLinkTextColor(Color.BLUE);
        Linkify.addLinks(link, Linkify.WEB_URLS);
    }

    private void initializeSubView(){
        TextView title = view.findViewById(R.id.title);
        title.setText(mMaterial.getTitle());

        TextView comment = view.findViewById(R.id.comment);
        comment.setText(mMaterial.getComment());
    }

    private void show() {
        File pdfFile = new File(Constants.STORAGE_PATH + mMaterial.getTitle() + ".pdf");
        if (pdfFile.exists() && getContext() != null) {
            Uri path = FileProvider.getUriForFile(getContext(),BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (getActivity() != null)
                startActivity(pdfIntent);
        }
    }
}