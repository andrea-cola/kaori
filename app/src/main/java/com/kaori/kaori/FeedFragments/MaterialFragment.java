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
import com.kaori.kaori.KaoriApp;
import com.kaori.kaori.KaoriChat;
import com.kaori.kaori.Model.Feedback;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.R;
import com.kaori.kaori.Utils.Constants;
import com.kaori.kaori.Utils.DataManager;
import com.kaori.kaori.Utils.FileManager;
import com.kaori.kaori.Utils.LogManager;

import java.io.File;
import java.util.List;

import static com.kaori.kaori.Utils.Constants.REMOTE_STORAGE_PATH;

/**
 * Show all the info about a single material.
 */
public class MaterialFragment extends Fragment {

    /**
     * Constants.
     */
    private final String USER_INTENT = "user";
    private final String PDF_EXT = ".pdf";

    /**
     * Variables.
     */
    private View view;
    private StorageReference storage;
    private Material mMaterial;
    private RecyclerView recyclerView;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.material_layout, container, false);
        storage = FirebaseStorage.getInstance().getReferenceFromUrl(REMOTE_STORAGE_PATH);
        initializeView();

        ((KaoriApp)getActivity()).shouldDisplayHomeUp();

        return view;
    }

    /**
     * Let the previous fragment to set the material to be shown.
     */
    public void setMaterial(Material material){
        this.mMaterial = material;
    }

    /**
     * Initialize the view on the base of the status of the current material.
     */
    private void initializeView(){
        LinearLayout linearLayout = view.findViewById(R.id.linearLayout);
        if(getActivity() != null) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (mMaterial.getType().equalsIgnoreCase(Constants.LIBRO)) {
                linearLayout.addView(inflater.inflate(R.layout.libro_layout, null));
                setBookLayout();
            } else if (mMaterial.getType().equalsIgnoreCase(Constants.FILE)) {
                linearLayout.addView(inflater.inflate(R.layout.file_layout, null));
                setFileLayout();
            } else if (mMaterial.getType().equalsIgnoreCase(Constants.URL)) {
                linearLayout.addView(inflater.inflate(R.layout.link_layout, null));
                setLinkLayout();
            }
            initializeSubView();
        }
    }

    /**
     * Set book sub layout.
     */
    private void setBookLayout(){
        TextView author = view.findViewById(R.id.author);
        author.setText(mMaterial.getProfessors().get(0));
        if(!mMaterial.getUser().getUid().equalsIgnoreCase(DataManager.getInstance().getUser().getUid()))
            view.findViewById(R.id.button).setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), KaoriChat.class);
                intent.putExtra(USER_INTENT, mMaterial.getUser());
                startActivity(intent);
            });
    }

    /**
     * Set file sub layout.
     */
    private void setFileLayout(){
        Button downloadButton = view.findViewById(R.id.button);
        TextView edittext = view.findViewById(R.id.feedbackEdittext);
        downloadButton.setOnClickListener(v -> storage.child(Constants.STORAGE_PATH_UPLOADS + mMaterial.getUser().getName() + "_" + mMaterial.getTitle() + ".pdf").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String path = Constants.INTERNAL_STORAGE_PATH + mMaterial.getTitle() + PDF_EXT;
                File pdfFile = new File(path);
                if (!pdfFile.exists() && new FileManager(mMaterial.getTitle(), uri, getActivity(), getContext()).download())
                    show();
                else
                    show();
            }
        }));

        view.findViewById(R.id.feedbackButton).setOnClickListener(view -> {
            Feedback feedback = new Feedback();
            feedback.setText(edittext.getText().toString());
            feedback.setTimestamp(Timestamp.now());
            feedback.setUser(DataManager.getInstance().getMiniUser());
            mMaterial.getFeedbacks().add(feedback);
            recyclerView.getAdapter().notifyDataSetChanged();

            DataManager.getInstance().postComment(mMaterial);
        });

        recyclerView = view.findViewById(R.id.feedbackList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(mMaterial.getFeedbacks());
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void setLinkLayout(){
        TextView edittext = view.findViewById(R.id.feedbackEdittext);
        TextView author = view.findViewById(R.id.author);
        author.setText(mMaterial.getUser().getName());

        view.findViewById(R.id.button).setOnClickListener(view->{
            Uri link = Uri.parse("http://" + mMaterial.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, link);
            startActivity(intent);
        });

        view.findViewById(R.id.edittext_button).setOnClickListener(view -> {
            Feedback feedback = new Feedback();
            feedback.setText(edittext.getText().toString());
            feedback.setTimestamp(Timestamp.now());
            feedback.setUser(DataManager.getInstance().getMiniUser());
            mMaterial.getFeedbacks().add(feedback);
            recyclerView.getAdapter().notifyDataSetChanged();

            DataManager.getInstance().postComment(mMaterial);
        });

        recyclerView = view.findViewById(R.id.feedbackList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(mMaterial.getFeedbacks());
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void initializeSubView(){
        ((TextView)view.findViewById(R.id.title)).setText(mMaterial.getTitle());
        ((TextView)view.findViewById(R.id.course)).setText(mMaterial.getCourse());
        ((TextView)view.findViewById(R.id.author)).setText(mMaterial.getUser().getName());
        ((TextView)view.findViewById(R.id.date)).setText(Constants.dateFormat2.format(mMaterial.getTimestamp().toDate()));

        String exams = "";
        for(String e : mMaterial.getExams())
             exams = e + ", ";
        ((TextView)view.findViewById(R.id.exams)).setText(exams.substring(0, exams.length() - 2));
    }

    private void show() {
        File pdfFile = new File(Constants.INTERNAL_STORAGE_PATH + mMaterial.getTitle() + PDF_EXT);
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
            holder.timestamp.setText(Constants.dateFormat2.format(feedbackList.get(i).getTimestamp().toDate()));
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