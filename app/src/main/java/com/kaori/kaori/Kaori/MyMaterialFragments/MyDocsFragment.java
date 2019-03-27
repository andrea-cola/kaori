package com.kaori.kaori.Kaori.MyMaterialFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaori.kaori.App;
import com.kaori.kaori.Constants;
import com.kaori.kaori.Kaori.HomeFragments.StarredAdapter;
import com.kaori.kaori.R;
import com.kaori.kaori.Services.DataManager;

public class MyDocsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_layout, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new StarredAdapter(DataManager.getInstance().getStarredDocuments(), Constants.FILE));

        ((TextView)view.findViewById(R.id.empty_view_text)).setText(R.string.empty_text_my_docs);
        App.setEmptyView(view.findViewById(R.id.empty_view));
        DataManager.getInstance().downloadStarredDocs(recyclerView);

        return view;
    }

}
