package com.kaori.kaori.Utils;

import android.content.Context;
import android.net.Uri;
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
import com.kaori.kaori.Kaori;
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
     * URL constants.
     */
    private final static String BASE_URL = "http://kaori.andreacola.io/api/";
    private final static String URL_FEED = "feed/";
    private final static String URL_EXAMS = "exams/";
    private final static String URL_USER = "user/";
    private final static String URL_SEARCH = "search/";

    /**
     * Singleton instance.
     */
    private static DataManager dataManager;

    /**
     * Volley utils.
     */
    private Gson gson = new Gson();
    private RequestQueue queue;

    /**
     * Data.
     */
    private User user; // the current logged in user.
    private ArrayList<Material> feedElements; // materials showed in the feed.
    private ArrayList<String> allExams; // all exams compatible with my graduation course and university.

    /**
     * Request options for Glide.
     */
    private RequestOptions getGlideRequestOptionsCenter;
    private RequestOptions getGlideRequestOptionsCircle;

    private DataManager(Context context) {
        user = new User();
        allExams = new ArrayList<>();
        feedElements = new ArrayList<>();

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<String> getAllExams() {
        return allExams;
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

    private String urlGeneratorFeedRequest(String url, List<String> params){
        if(params != null && params.size() > 0){
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
        StringRequest request = new StringRequest(Request.Method.GET, urlGeneratorFeedRequest(BASE_URL + URL_FEED, user.getExams()),
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
                error -> LogManager.getInstance().printConsoleError("Feed: " + error.toString() + " " + error.networkResponse.statusCode));
        queue.add(request);
    }

    /**
     * Load all allExams from the database.
     */
    private void loadAllExams(){
        Uri url = Uri.parse(BASE_URL + URL_EXAMS + "?university=" + user.getUniversity() + "&course=" + user.getCourse());
        StringRequest request = new StringRequest(Request.Method.GET, url.toString(),
            response -> {
                allExams = gson.fromJson(response, new TypeToken<ArrayList<String>>(){}.getType());
                LogManager.getInstance().printConsoleMessage("All exams are loaded.");
            },
            error -> {
                //TODO
                LogManager.getInstance().printConsoleError("All Exams: " + error.toString() + " " + error.networkResponse.statusCode);
            });
        queue.add(request);
    }

    /**
     * Load the profile when the app starts.
     * @param uid of the logged user.
     * @param kaori activity to run KaoriApp.
     */
    public void loadUserProfile(String uid, Kaori kaori){
        String url = BASE_URL + URL_USER + "?uid=" + uid;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    user = gson.fromJson(response, new TypeToken<User>(){}.getType());
                    if(user != null) {
                        loadAllExams();
                        LogManager.getInstance().printConsoleMessage("Profile loaded.");
                        kaori.startApp();
                    }
                    else {
                        LogManager.getInstance().printConsoleMessage("Profile not loaded.");
                        kaori.startLogin();
                    }
                },
                error -> {
                    LogManager.getInstance().printConsoleError(error.toString());
                    // TODO: segnalare che l'utente non è stato caricato e quindi bisogna rifare.
                });
        queue.add(request);
    }

    public void queryMaterials(String query){
        Uri url = Uri.parse(BASE_URL + URL_SEARCH + "?university=" + user.getUniversity() + "&query=" + query);
        LogManager.getInstance().printConsoleMessage(url.toString());
        StringRequest request = new StringRequest(Request.Method.GET, url.toString(),
                response -> {
                    //allExams = gson.fromJson(response, new TypeToken<ArrayList<String>>(){}.getType());
                    LogManager.getInstance().printConsoleMessage(response);
                },
                error -> {
                    //TODO
                    LogManager.getInstance().printConsoleError("All Exams: " + error.toString() + " " + error.networkResponse.statusCode);
                });
        queue.add(request);
    }

}
