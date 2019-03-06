package com.kaori.kaori.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kaori.kaori.Kaori;
import com.kaori.kaori.Model.Course;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final static String URL_BOOK = "book/";
    private final static String URL_SEARCH = "search/";
    private final static String URL_UNIVERSITIES = "universities/";
    private final static String URL_COURSES = "courses/";
    private final static String URL_DOC = "document/";

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
    private ArrayList<Document> feedElements; // materials showed in the feed.
    private ArrayList<String> allExams; // all exams compatible with my graduation course and university.
    private ArrayList<String> allUniversities; // all universities.
    private ArrayList<Course> allCourses; // all courses.
    private ArrayList<Document> searchElements; // list used in search fragment to update quickly the listview
    private ArrayList<Document> myFiles; // list of all my materials.

    /**
     * Request options for Glide.
     */
    private RequestOptions glideRequestOptionsCenter;
    private RequestOptions glideRequestOptionsCircle;

    private DataManager(Context context) {
        user = new User();
        allExams = new ArrayList<>();
        allUniversities = new ArrayList<>();
        allCourses = new ArrayList<>();
        feedElements = new ArrayList<>();
        searchElements = new ArrayList<>();
        myFiles = new ArrayList<>();

        glideRequestOptionsCenter = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.placeholder);

        glideRequestOptionsCircle = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .error(R.drawable.placeholder);

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

    public ArrayList<String> getAllExams() {
        return allExams;
    }

    public MiniUser getMiniUser() {
        return new MiniUser(user.getUid(), user.getName(), user.getPhotosUrl(), user.getTokenID());
    }

    public void clean(Context context) {
        dataManager = new DataManager(context);
    }

    public void postComment(Document material) {
        FirebaseFirestore.getInstance().collection(Constants.DB_COLL_MATERIALS)
                .document(material.getId())
                .set(material);
    }

    public ArrayList<Document> getFeedElements() {
        return feedElements;
    }

    public ArrayList<Document> getSearchElements() {
        return searchElements;
    }

    public ArrayList<String> getAllUniversities() {
        return allUniversities;
    }

    public ArrayList<String> getAllCourses(String university) {
        ArrayList<String> courses = new ArrayList<>();
        for(Course c : allCourses)
            if(c.getUniversity().equalsIgnoreCase(university))
                courses.add(c.getName());
        return courses;
    }

    private String urlGeneratorFeedRequest(String url, List<String> params){
        if(params != null && params.size() > 0){
            url = url + "?exams=" + params.get(0);
            for (int i = 1; i < params.size(); i++)
                url = url + "&exams=" + params.get(i);
        }
        return url;
    }

    public ArrayList<Document> getMyFiles() {
        return myFiles;
    }

    /**
     * Load the element of the feed.
     * @param list where the elements are loaded.
     */
    public void loadFeed(RecyclerView list, View view){
        StringRequest request = new StringRequest(Request.Method.GET, urlGeneratorFeedRequest(BASE_URL + URL_FEED, user.getExams()),
                response -> {
                    feedElements.clear();
                    feedElements.addAll(gson.fromJson(response, new TypeToken<ArrayList<Document>>(){}.getType()));
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
     * Load the element of the feed.
     * @param list where the elements are loaded.
     */
    public void loadMyDocs(RecyclerView list, View view){
        StringRequest request = new StringRequest(Request.Method.GET, urlGeneratorFeedRequest(BASE_URL + URL_FEED, user.getExams()),
                response -> {
                    feedElements.clear();
                    feedElements.addAll(gson.fromJson(response, new TypeToken<ArrayList<Document>>(){}.getType()));
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
     * Load the element of the feed.
     * @param list where the elements are loaded.
     */
    public void loadMyBooks(RecyclerView list, View view){
        StringRequest request = new StringRequest(Request.Method.GET, urlGeneratorFeedRequest(BASE_URL + URL_FEED, user.getExams()),
                response -> {
                    feedElements.clear();
                    feedElements.addAll(gson.fromJson(response, new TypeToken<ArrayList<Document>>(){}.getType()));
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
     * Load all exams from the database taking into account
     * the university and the course of the user.
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
     * Load all universities from the database.
     */
    private void loadAllUniversities(){
        Uri url = Uri.parse(BASE_URL + URL_UNIVERSITIES);
        StringRequest request = new StringRequest(Request.Method.GET, url.toString(),
                response -> {
                    allUniversities = gson.fromJson(response, new TypeToken<ArrayList<String>>(){}.getType());
                    LogManager.getInstance().printConsoleMessage("All universites are loaded." + "\n" + allUniversities.toString());
                },
                error -> {
                    //TODO
                    LogManager.getInstance().printConsoleError("All Universities: " + error.toString() + " " + error.networkResponse.statusCode);
                });
        queue.add(request);
    }

    /**
     * Load all courses from the database.
     */
    private void loadAllCourses(){
        Uri url = Uri.parse(BASE_URL + URL_COURSES);
        StringRequest request = new StringRequest(Request.Method.GET, url.toString(),
                response -> {
                    allCourses = gson.fromJson(response, new TypeToken<ArrayList<Course>>(){}.getType());
                    LogManager.getInstance().printConsoleMessage("All courses are loaded." + "\n" + allCourses.toString());
                },
                error -> {
                    //TODO
                    LogManager.getInstance().printConsoleError("All Courses: " + error.toString() + " " + error.networkResponse.statusCode);
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
                        loadAllUniversities();
                        loadAllCourses();
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

    /**
     * Makes a search in the database.
     */
    public void queryMaterials(String query, RecyclerView list, View emptyView){
        Uri url = Uri.parse(BASE_URL + URL_SEARCH + "?university=" + user.getUniversity() + "&query=" + query);
        StringRequest request = new StringRequest(Request.Method.GET, url.toString(),
                response -> {
                    searchElements.clear();
                    searchElements.addAll(gson.fromJson(response, new TypeToken<ArrayList<Document>>(){}.getType()));

                    if(searchElements.size() > 0) {
                        list.getAdapter().notifyDataSetChanged();
                        emptyView.setVisibility(View.GONE);
                    }
                    else {
                        ((TextView)emptyView.findViewById(R.id.empty_view_text)).setText(R.string.search_empty_view_text);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    //TODO
                    ((TextView)emptyView.findViewById(R.id.empty_view_text)).setText(R.string.search_empty_view_text);
                    emptyView.setVisibility(View.VISIBLE);
                    LogManager.getInstance().printConsoleError("All Exams: " + error.toString() + " " + error.networkResponse.statusCode);
                });
        queue.add(request);
    }

    /**
     * Update the user in the database.
     */
    private void updateUser(){
        Uri url = Uri.parse(BASE_URL + URL_USER);
        LogManager.getInstance().printConsoleError(url.toString());
        StringRequest request = new StringRequest(Request.Method.POST, url.toString(),
                response -> {
                    if(response.equalsIgnoreCase("1"))
                        LogManager.getInstance().showVisualMessage("Piano di studi modificato.");
                    else
                        LogManager.getInstance().showVisualMessage("Aggiornamento fallito, riprovare.");
                },
                error -> {
                    LogManager.getInstance().printConsoleError(error.networkResponse.statusCode + "");
                    LogManager.getInstance().showVisualMessage("Aggiornamento fallito, riprovare.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String>  params = new HashMap<>();
                params.put("user", gson.toJson(user));
                params.put("uid", user.getUid());
                return params;
            }
        };
        queue.add(request);
    }

    /**
     * Update the user in the database, but if bitmap is different from
     * null is load the bitmap in the Firebase storage.
     */
    public void updateUser(Bitmap bitmap) {
        if(bitmap != null)
            uploadImageOnServer(bitmap);
        else
            updateUser();
    }

    /**
     * Use Glide to load the image into the Imageview.
     */
    public void loadImageIntoView(Object uri, ImageView imageView, Context context) {
        if(context != null)
            Glide.with(context)
                    .load(uri)
                    .apply(glideRequestOptionsCircle)
                    .into(imageView);
    }

    /**
     * Upload the image in the Firebase storage.
     */
    private void uploadImageOnServer(Bitmap bitmap) {
        LogManager.getInstance().printConsoleMessage("uploadProfileImageOnTheServer");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_PROFILE_IMAGES + user.getUid());
        mStorage.putBytes(baos.toByteArray()).addOnSuccessListener(taskSnapshot -> mStorage.getDownloadUrl()
            .addOnSuccessListener(uri -> {
                LogManager.getInstance().printConsoleMessage("Il tuo profilo è stato aggiornato.");
                user.setPhotosUrl(uri.toString());
                updateUser(null);
            }))
            .addOnFailureListener(e -> LogManager.getInstance().showVisualError(e, "Il tuo profilo non è stato aggiornato."));
    }

    /**
     * Load user uploads from the database.
     */
    public void loadMyFiles(RecyclerView list, View view) {
        String url = BASE_URL + URL_SEARCH + "?uid=" + user.getUid();
        LogManager.getInstance().printConsoleMessage(url);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    myFiles.clear();
                    myFiles.addAll(gson.fromJson(response, new TypeToken<ArrayList<Document>>(){}.getType()));
                    LogManager.getInstance().printConsoleMessage(myFiles.toString());
                    if(myFiles != null && myFiles.size() > 0) {
                        list.getAdapter().notifyDataSetChanged();
                        view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                        LogManager.getInstance().printConsoleMessage("Files loaded.");
                    }
                    else {
                        view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
                        view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                        LogManager.getInstance().printConsoleMessage("Files not loaded.");
                    }
                },
                error -> {
                    LogManager.getInstance().printConsoleError(error.toString());
                    // TODO: segnalare che l'utente non è stato caricato e quindi bisogna rifare.
                });
        queue.add(request);
    }

    /**
     * Update the user in the database.
     */
    public void uploadDocument(Document document){
        Uri url = Uri.parse(BASE_URL + URL_DOC);
        LogManager.getInstance().printConsoleError(url.toString());
        StringRequest request = new StringRequest(Request.Method.POST, url.toString(),
                response -> {
                    LogManager.getInstance().printConsoleMessage(response);
                },
                error -> {
                    LogManager.getInstance().printConsoleError(error.networkResponse.statusCode + "");
                    LogManager.getInstance().showVisualMessage("Aggiornamento fallito, riprovare.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String>  params = new HashMap<>();
                params.put("document", gson.toJson(document));
                return params;
            }
        };
        queue.add(request);
    }

}