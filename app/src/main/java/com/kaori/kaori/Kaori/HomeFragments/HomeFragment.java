package com.kaori.kaori.Kaori.HomeFragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaori.kaori.App;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;

import java.util.List;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_layout, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerAdapter(DataManager.getInstance().getFeedElements()));

        ((TextView)view.findViewById(R.id.empty_view_text)).setText(R.string.empty_text_feed);
        App.setEmptyView(view.findViewById(R.id.empty_view));
        DataManager.getInstance().downloadFeed(recyclerView);

        return view;
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

        private final int MODIFIED = 4;

        private List<Document> materials;

        /*package-private*/ RecyclerAdapter(List<Document> materials){
            this.materials = materials;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case Constants.BOOK:
                    return new BookHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_book_card, parent, false));
                case Constants.FILE:
                    return new DocumentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_doc_card, parent, false));
                case Constants.URL:
                    return new DocumentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_url_card, parent, false));
                default:
                    return new UpdateHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card_update, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, int i) {
            if(getItemViewType(i) != MODIFIED) {
                if(getItemViewType(i) == Constants.BOOK)
                    setBookHolderView(holder, materials.get(i));
                else if(getItemViewType(i) == Constants.URL)
                    setUrlHolderView(holder, materials.get(i));
                else
                    setDocumentHolderView(holder, materials.get(i));
            } else {
                holder.author.setText(materials.get(i).getUser().getName());
                holder.date.setText(Constants.getFormattedDate(materials.get(i).getTimestamp()));
                String details = materials.get(i).getUser().getName().split(" ")[0];

                DataManager.getInstance().loadImageIntoView(materials.get(i).getUser().getThumbnail(), holder.authorIcon, getContext());

                SpannableStringBuilder t1 = new SpannableStringBuilder(details + " has updated the " + Constants.translateTypeCode(materials.get(i).getSubtype()) + " ");
                SpannableStringBuilder t2 = new SpannableStringBuilder(materials.get(i).getTitle());
                t2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, t2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                t2.setSpan(new StyleSpan(Typeface.BOLD), 0, t2.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                ((UpdateHolder)holder).details.setText(TextUtils.concat(t1, t2), TextView.BufferType.SPANNABLE);

                holder.cardView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), MaterialActivity.class);
                    intent.putExtra("document", materials.get(i));
                    startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return materials.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(materials.get(position).getModified())
                return MODIFIED;
            return materials.get(position).getSubtype();
        }

        private void setBookHolderView(Holder holder, Document item){
            holder.title.setText(item.getTitle());
            holder.author.setText(item.getUser().getName());
            holder.date.setText(Constants.getFormattedDate(item.getTimestamp()));

            DataManager.getInstance().loadImageIntoView(item.getUser().getThumbnail(), holder.authorIcon, getContext());

            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MaterialActivity.class);
                intent.putExtra("document", item);
                startActivity(intent);
            });

            holder.exams.setText(buildExamsString(item.getExams()));

            ((BookHolder) holder).price.setText("€ " + String.format("%.2f", item.getPrice()));
            DataManager.getInstance().loadImageIntoBackgroundView(App.getDrawableFromRes(R.drawable.background_book), holder.background, getContext());
        }

        private void setUrlHolderView(Holder holder, Document item){
            holder.title.setText(item.getTitle());
            holder.author.setText(item.getUser().getName());
            holder.date.setText(Constants.getFormattedDate(item.getTimestamp()));

            DataManager.getInstance().loadImageIntoView(item.getUser().getThumbnail(), holder.authorIcon, getContext());

            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MaterialActivity.class);
                intent.putExtra("document", item);
                startActivity(intent);
            });

            holder.exams.setText(buildExamsString(item.getExams()));

            DataManager.getInstance().loadImageIntoBackgroundView(
                    App.getDrawableFromRes(R.drawable.background_cloud), holder.background,
                    getContext());
            ((DocumentHolder) holder).comments.setText(item.getFeedbacks().size() + " feedback");
        }

        private void setDocumentHolderView(Holder holder, Document item){
            holder.title.setText(item.getTitle());
            holder.author.setText(item.getUser().getName());
            holder.date.setText(Constants.getFormattedDate(item.getTimestamp()));

            DataManager.getInstance().loadImageIntoView(item.getUser().getThumbnail(), holder.authorIcon, getContext());

            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MaterialActivity.class);
                intent.putExtra("document", item);
                startActivity(intent);
            });

            holder.exams.setText(buildExamsString(item.getExams()));

            DataManager.getInstance().loadImageIntoBackgroundView(
                    App.getDrawableFromRes(R.drawable.background_file), holder.background,
                    getContext());
            ((DocumentHolder) holder).comments.setText(item.getFeedbacks().size() + " feedback");
        }

        private String buildExamsString(List<String> exams){
            String s = exams.get(0);

            for (int i = 1; i < exams.size(); i++)
                s +=  ", " + exams.get(i);
            return s;
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView title, author, date, exams;
            MaterialCardView cardView;
            ImageView authorIcon, background;

            /*package-private*/ Holder (View view) {
                super(view);
                exams = view.findViewById(R.id.exams);
                title = view.findViewById(R.id.title);
                author = view.findViewById(R.id.user_and_text);
                date = view.findViewById(R.id.date);
                cardView = view.findViewById(R.id.card_view);
                authorIcon = view.findViewById(R.id.profileImage);
                background = view.findViewById(R.id.background);
            }
        }

        class BookHolder extends Holder {
            TextView price;

            /*package-private*/ BookHolder (View view) {
                super(view);
                price = view.findViewById(R.id.price);
            }
        }

        class DocumentHolder extends Holder {
            TextView comments;

            DocumentHolder (View view) {
                super(view);
                comments = view.findViewById(R.id.comments);
            }
        }

        class UpdateHolder extends Holder {
            TextView details;

            /*package-private*/ UpdateHolder (View view) {
                super(view);
                details = view.findViewById(R.id.details);
            }
        }

    }
}