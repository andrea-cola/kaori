package com.kaori.kaori.Kaori.HomeFragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.kaori.kaori.App;
import com.kaori.kaori.BuildConfig;
import com.kaori.kaori.Chat.KaoriChat;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.Model.Feedback;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;
import com.kaori.kaori.Services.FileManager;
import com.kaori.kaori.Services.LogManager;

import java.io.File;
import java.util.Date;
import java.util.List;

public class MaterialActivity extends AppCompatActivity {

    private boolean feedbacks = false;
    private Document material;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MaterialStyle);
        setContentView(R.layout.material_layout);
        LogManager.initialize(findViewById(R.id.coordinator));
        material = (Document) getIntent().getSerializableExtra("document");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        fab = findViewById(R.id.button);
        setCommonInfo();

        if(material.getSubtype() == Constants.FILE)
            setDocumentLayout();
        else if(material.getSubtype() == Constants.URL)
            setUrlLayout();
        else
            setBookLayout();
    }

    private void setCommonInfo(){
        ((TextView)findViewById(R.id.type)).setText(Constants.translateTypeCode(material.getSubtype()).toUpperCase());
        ((TextView)findViewById(R.id.title)).setText(material.getTitle());
        ((TextView)findViewById(R.id.course)).setText(material.getCourse());
        ((TextView)findViewById(R.id.author)).setText("di " + material.getUser().getName());
        ((TextView)findViewById(R.id.note)).setText(material.getNote());
        ((TextView)findViewById(R.id.date)).setText(Constants.dateFormat.format(new Date(material.getTimestamp() * Constants.constantDate)));
        findViewById(R.id.back_button).setOnClickListener(view -> super.onBackPressed());
        String exams = "";
        for(String e : material.getExams())
            exams = exams + e + ", ";
        ((TextView)findViewById(R.id.exams)).setText(exams.substring(0, exams.length() - 2));

        CheckBox star = findViewById(R.id.starred);
        if(DataManager.getInstance().getUser().containsStarred(material.getId()))
            star.setChecked(true);
        star.setOnCheckedChangeListener((buttonView, isChecked) -> DataManager.getInstance().setStarredDocument(material, isChecked));
    }

    private void setDocumentLayout(){
        fab.setImageDrawable(App.getDrawableFromRes(R.drawable.ic_file_download_black_24dp));
        fab.setOnClickListener(v -> {
            String path = Constants.INTERNAL_STORAGE_PATH + material.getTitle() + ".pdf";
            File pdfFile = new File(path);
            if (!pdfFile.exists() && new FileManager(material.getTitle(), Uri.parse(material.getUrl()), this).download())
                show();
            else
                show();
        });
        setCommonLayout();
    }

    private void setUrlLayout(){
        ((ImageView)findViewById(R.id.background)).setImageDrawable(App.getDrawableFromRes(R.drawable.background_cloud));
        fab.setImageDrawable(App.getDrawableFromRes(R.drawable.ic_public_black_24dp));
        findViewById(R.id.button).setOnClickListener(view -> {
            Uri link = Uri.parse("http://" + material.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, link);
            startActivity(intent);
        });
        setCommonLayout();
    }

    private void setBookLayout(){
        ((ImageView)findViewById(R.id.background)).setImageDrawable(App.getDrawableFromRes(R.drawable.background_book));
        findViewById(R.id.button).setVisibility(View.GONE);
        if(material.getUser().getUid().equalsIgnoreCase(DataManager.getInstance().getUser().getUid())) {
            findViewById(R.id.libro_file).setVisibility(View.VISIBLE);
            findViewById(R.id.owner_button).setOnClickListener(view -> {
                Intent intent = new Intent(this, KaoriChat.class);
                intent.putExtra("user", material.getUser());
                startActivity(intent);
            });
        }
    }

    private void setCommonLayout(){
        findViewById(R.id.doc_file).setVisibility(View.VISIBLE);

        Button showHide = findViewById(R.id.show_hide_feedbacks);
        Group group = findViewById(R.id.feedback_group);
        showHide.setOnClickListener(view -> {
            if(feedbacks){
                showHide.setText("Mostra feedbacks");
                group.setVisibility(View.GONE);
            } else {
                showHide.setText("Nascondi feedbacks");
                group.setVisibility(View.VISIBLE);
            }
            feedbacks = !feedbacks;
        });

        RecyclerView recyclerView = findViewById(R.id.feedbackList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(this, material.getFeedbacks());
        recyclerView.setAdapter(recyclerAdapter);

        TextInputEditText feedbackEditText = findViewById(R.id.feedbackEditText);
        findViewById(R.id.feedbackButton).setOnClickListener(view -> {
            Feedback feedback = new Feedback();
            feedback.setText(String.valueOf(feedbackEditText.getText()));
            feedback.setTimestamp(Timestamp.now().getSeconds());
            feedback.setUser(DataManager.getInstance().getMiniUser());
            recyclerView.getAdapter().notifyDataSetChanged();

            DataManager.getInstance().postComment(material, feedback);
        });
    }

    private void show() {
        File pdfFile = new File(Constants.INTERNAL_STORAGE_PATH + material.getTitle() + ".pdf");
        if (pdfFile.exists()) {
            Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(pdfIntent);
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        private List<Feedback> feedbackList;
        private Activity activity;

        /*package-private*/ RecyclerAdapter(Activity activity, List<Feedback> feedbackList){
            this.feedbackList = feedbackList;
            this.activity = activity;
        }

        @NonNull
        @Override
        public RecyclerAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            return new RecyclerAdapter.Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.Holder holder, int i) {
            DataManager.getInstance().loadImageIntoView(feedbackList.get(i).getUser().getThumbnail(), holder.userImage, activity);
            holder.timestamp.setText(Constants.dateFormat.format(feedbackList.get(i).getTimestamp()));
            holder.content.setText(Html.fromHtml(
                    "<b>" + feedbackList.get(i).getUser().getName() + "</b> "
                            + feedbackList.get(i).getText()));
        }

        @Override
        public int getItemCount() {
            return feedbackList.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            ImageView userImage;
            TextView content, timestamp;

            Holder (View v) {
                super(v);
                content = v.findViewById(R.id.user_and_text);
                timestamp = v.findViewById(R.id.date);
                userImage = v.findViewById(R.id.profile_image);
            }
        }
    }

}
