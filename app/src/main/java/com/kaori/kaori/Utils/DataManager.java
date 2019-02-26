package com.kaori.kaori.Utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kaori.kaori.Model.Material;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class that represent a store
 * for the most important and common data.
 */
public class DataManager {

    /**
     * Singleton instance.
     */
    private static DataManager dataManager;

    /**
     * Feeds variables.
     */
    private ArrayList<Material> feedElements;

    private final static String BASE_URL = "http://kaori.andreacola.io/api/";
    private final static String URL_FEED = "feed/";

    private Gson gson = new Gson();
    private RequestQueue queue;

    private boolean isAuthenticated;
    private User user;
    private ArrayList<String> exams;
    private ArrayList<Material> allMaterials;

    /**
     * Request options for Glide.
     */
    private RequestOptions getGlideRequestOptionsCenter;
    private RequestOptions getGlideRequestOptionsCircle;

    private DataManager(Context context) {
        isAuthenticated = false;
        user = new User();
        exams = new ArrayList<>();
        feedElements = new ArrayList<>();
        allMaterials = new ArrayList<>();

        getGlideRequestOptionsCenter = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        //.error(R.drawable.placeholder);

        getGlideRequestOptionsCircle = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop();
        //.error(R.drawable.placeholder);

        queue = Volley.newRequestQueue(context);
    }

    public static DataManager getInstance(){
        return dataManager;
    }

    public static DataManager getInstance(Context context) {
        if (dataManager == null)
            dataManager = new DataManager(context);
        return dataManager;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<String> getExams() {
        return exams;
    }

    public RequestOptions getGetGlideRequestOptionsCircle() {
        return getGlideRequestOptionsCircle;
    }

    public RequestOptions getGetGlideRequestOptionsCenter() {
        return getGlideRequestOptionsCenter;
    }

    public MiniUser getMiniUser() {
        return new MiniUser(user.getUid(), user.getName(), user.getPhotosUrl(), user.getTokenID());
    }

    public void clean(Context context) {
        dataManager = new DataManager(context);
    }

    public void postComment(Material material) {
        FirebaseFirestore.getInstance().collection(Constants.DB_COLL_MATERIALS)
                .document(material.getId())
                .set(material);
    }

    public ArrayList<Material> getFeedElements() {
        return feedElements;
    }

    public ArrayList<Material> getAllMaterials() {
        return allMaterials;
    }

    public void setAllMaterials(ArrayList<Material> allMaterials) {
        this.allMaterials = allMaterials;
    }

    private String urlGenerator(String url, List<String> params){
        if(params.size() > 0){
            url = url + "?exams=" + params.get(0);
            for (int i = 1; i < params.size(); i++)
                url = url + "&exams=" + params.get(i);
        }
        return url;
    }

    /**
     * Load the element of the feed.
     * @param list where the elements are loaded.
     */
    public void loadFeed(RecyclerView list, View view){
        StringRequest request = new StringRequest(Request.Method.GET, urlGenerator(BASE_URL + URL_FEED, user.getExams()),
                response -> {
                    feedElements.clear();
                    feedElements.addAll(gson.fromJson(response, new TypeToken<ArrayList<Material>>(){}.getType()));
                    list.getAdapter().notifyDataSetChanged();
                    view.findViewById(R.id.wait_layout).setVisibility(View.GONE);

                    if(feedElements.size() == 0) {
                        ((TextView)view.findViewById(R.id.empty_view_text)).setText(R.string.feed_empty_view_text);
                        view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                    }
                },
                error -> LogManager.getInstance().printConsoleError(error.toString()));
        queue.add(request);
    }

}
