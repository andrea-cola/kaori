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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.BuildConfig;
import com.kaori.kaori.KaoriChat;
import com.kaori.kaori.Model.Feedback;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.FileManager;

import java.io.File;
import java.util.List;

public class MaterialFragment extends Fragment {

    private View view;
    private StorageReference storage;
    private Material mMaterial;
    private RecyclerView recyclerView;

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
        if(!mMaterial.getUser().getUid().equalsIgnoreCase(DataManager.getInstance().getUser().getUid()))
            view.findViewById(R.id.button).setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), KaoriChat.class);
                intent.putExtra("user", mMaterial.getUser());
                startActivity(intent);
            });
    }

    private void setFileLayout(){
        Button downloadButton = view.findViewById(R.id.downloadButton);
        TextView edittext = view.findViewById(R.id.edittext);
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

        view.findViewById(R.id.edittext_button).setOnClickListener(view -> {
            Feedback feedback = new Feedback();
            feedback.setText(edittext.getText().toString());
            feedback.setTimestamp(Timestamp.now());
            feedback.setUser(DataManager.getInstance().getMiniUser());
            mMaterial.getFeedbacks().add(feedback);
            recyclerView.getAdapter().notifyDataSetChanged();

            DataManager.getInstance().postComment(mMaterial);
        });

        recyclerView = view.findViewById(R.id.feedback);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(mMaterial.getFeedbacks());
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void setLinkLayout(){
        TextView edittext = view.findViewById(R.id.edittext);
        TextView author = view.findViewById(R.id.author);
        author.setText(mMaterial.getUser().getName());
        TextView link = view.findViewById(R.id.url);
        link.setText(mMaterial.getUrl());
        link.setLinkTextColor(Color.BLUE);
        Linkify.addLinks(link, Linkify.WEB_URLS);

        view.findViewById(R.id.edittext_button).setOnClickListener(view -> {
            Feedback feedback = new Feedback();
            feedback.setText(edittext.getText().toString());
            feedback.setTimestamp(Timestamp.now());
            feedback.setUser(DataManager.getInstance().getMiniUser());
            mMaterial.getFeedbacks().add(feedback);
            recyclerView.getAdapter().notifyDataSetChanged();

            DataManager.getInstance().postComment(mMaterial);
        });

        recyclerView = view.findViewById(R.id.feedback);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(mMaterial.getFeedbacks());
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void initializeSubView(){
        ((TextView)view.findViewById(R.id.title)).setText(mMaterial.getTitle());
        ((TextView)view.findViewById(R.id.comment)).setText(mMaterial.getComment());
    }

    private void show() {
        File pdfFile = new File(Constants.STORAGE_PATH + mMaterial.getTitle() + ".pdf");
        if (pdfFile.exists() && getContext() != null) {
            Uri path = FileProvider.getUriForFile(getContext(),BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(pdfIntent);
        }
    }

    private void loadImage(ImageView imageView, String url){
        if(getContext() != null)
            Glide.with(getContext())
                    .load(url)
                    .apply(DataManager.getInstance().getGetGlideRequestOptionsCircle())
                    .into(imageView);
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        private List<Feedback> feedbackList;

        /*package-private*/ RecyclerAdapter(List<Feedback> feedbackList){
            this.feedbackList = feedbackList;
        }

        @NonNull
        @Override
        public RecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new RecyclerAdapter.Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_left, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.Holder holder, int i) {
            holder.timestamp.setText(Constants.dateFormat.format(feedbackList.get(i).getTimestamp().toDate()));
            loadImage(holder.userImage, feedbackList.get(i).getUser().getThumbnail());
            holder.content.setText(feedbackList.get(i).getText());
        }

        @Override
        public int getItemCount() {
            return feedbackList.size();
        }

        /*package-private*/ class Holder extends RecyclerView.ViewHolder {
            ImageView userImage;
            TextView content, timestamp;

            /*package-private*/ Holder (View v) {
                super(v);
                content = v.findViewById(R.id.message_content);
                timestamp = v.findViewById(R.id.message_time);
                userImage = v.findViewById(R.id.user_profile_image);
            }
        }
    }
}