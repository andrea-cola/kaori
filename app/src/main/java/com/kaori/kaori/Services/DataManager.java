package com.kaori.kaori.Services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kaori.kaori.App;
import com.kaori.kaori.Constants;
import com.kaori.kaori.MainActivity;
import com.kaori.kaori.Model.Chat;
import com.kaori.kaori.Model.Course;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.Model.Message;
import com.kaori.kaori.Model.MiniUser;
import com.kaori.kaori.Model.Position;
import com.kaori.kaori.Model.User;
import com.kaori.kaori.R;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataManager {

    /**
     * URL constants.
     */
    private final static String BASE_URL = "http://kaori.andreacola.io/api/";
    private final static String URL_FEED = "feed/";
    private final static String URL_STARRED = "starred/";
    private final static String URL_EXAMS = "exams/";
    private final static String URL_USER = "user/";
    private final static String URL_SEARCH = "search/";
    private final static String URL_UNIVERSITIES = "universities/";
    private final static String URL_COURSES = "courses/";
    private final static String URL_DOC = "document/";
    private final static String URL_POSITION = "position/";
    private final static String URL_CHECK = "checkAuth/";
    private final static String URL_PROVIDER_VALIDATION = "providerValidation/";
    private final static String URL_TOKEN = "token/";
    private final static String URL_MESSAGE = "message/";
    private final static String URL_CHATS = "chats/";
    private final static String URL_POS_REMOVE = "positionDelete/";

    /**
     * Singleton instance.
     */
    @SuppressLint("StaticFieldLeak")
    private static DataManager dataManager;
    private Gson gson = new Gson();
    private RequestQueue queue;

    /**
     * Data variables.
     */
    private User user; // the current logged in user.
    private ArrayList<Document> feedElements; // materials showed in the feed.
    private ArrayList<String> allExams; // all exams compatible with my graduation course and university.
    private ArrayList<String> allUniversities; // all universities.
    private ArrayList<Course> allCourses; // all courses.
    private ArrayList<Document> searchElements; // list used in search fragment to update quickly the listview
    private ArrayList<Document> myFiles; // list of all my materials.
    private ArrayList<Document> starredDocuments;
    private ArrayList<Document> starredBooks;
    private ArrayList<Position> currentActivePositions; // list of all current active positions.
    private ArrayList<Chat> allChats; // list of all my chats.

    /**
     * Request options for Glide.
     */
    private RequestOptions glideRequestOptionsCenter;
    private RequestOptions glideRequestOptionsCircle;

    /* ------------------------------------------------------------------------------------------------------ */
    /* SINGLETON CONSTRUCTORS ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------ */

    private DataManager() {
        user = new User();
        allExams = new ArrayList<>();
        allUniversities = new ArrayList<>();
        allCourses = new ArrayList<>();
        feedElements = new ArrayList<>();
        searchElements = new ArrayList<>();
        myFiles = new ArrayList<>();
        starredDocuments = new ArrayList<>();
        starredBooks = new ArrayList<>();
        currentActivePositions = new ArrayList<>();
        allChats = new ArrayList<>();

        glideRequestOptionsCenter = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder_loading)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.placeholder_loading);

        glideRequestOptionsCircle = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder_loading)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .error(R.drawable.placeholder_loading);

        queue = Volley.newRequestQueue(App.getActiveContext());
    }

    public static void initialize() {
        dataManager = new DataManager();
    }

    public static DataManager getInstance(){
        return dataManager;
    }

    /* ------------------------------------------------------------------------------------------------------ */
    /* SETTER AND GETTERS ----------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------ */

    public User getUser() {
        return user;
    }

    public ArrayList<String> getAllExams() {
        return allExams;
    }

    public MiniUser getMiniUser() {
        MiniUser miniUser = new MiniUser();
        miniUser.setName(user.getName());
        miniUser.setUid(user.getUid());
        miniUser.setThumbnail(user.getPhotosUrl());
        return miniUser;
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

    public ArrayList<Document> getMyFiles() {
        return myFiles;
    }

    public ArrayList<Document> getStarredDocuments() {
        return starredDocuments;
    }

    public ArrayList<Document> getStarredBooks() {
        return starredBooks;
    }

    public ArrayList<Position> getCurrentActivePositions() {
        return currentActivePositions;
    }

    /* ------------------------------------------------------------------------------------------------------ */
    /* GENERIC FUNCTIONS ------------------------------------------------------------------------------------ */
    /* ------------------------------------------------------------------------------------------------------ */

    public void clean() {
        dataManager = new DataManager();
    }

    private String urlGenerator(String url, String... params){
        if(params != null && params.length > 0){
            url = url + "?p0=" + params[0];
            for (int i = 1; i < params.length; i++)
                url = url + "&p" + i + "=" + params[i];
        }
        return url;
    }

    public void updateUser(Bitmap bitmap) {
        if(bitmap != null)
            uploadProfileImageOnServer(bitmap);
        else
            uploadUser();
    }

    public void loadImageIntoView(Object uri, ImageView imageView, Context context) {
        if(context != null)
            Glide.with(context)
                    .load(uri)
                    .apply(glideRequestOptionsCircle)
                    .into(imageView);
    }

    public void loadImageIntoBackgroundView(Object uri, ImageView imageView, Context context) {
        if(context != null)
            Glide.with(context)
                    .load(uri)
                    .apply(glideRequestOptionsCenter)
                    .into(imageView);
    }

    private void makeCustomPostRequest(final Uri url, final Response.Listener<String> listener, final Response.ErrorListener errorListener, Object... params){
        LogManager.getInstance().printConsoleMessage("POST -> " + url.toString());
        StringRequest request = new StringRequest(Request.Method.POST, url.toString(), listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> p = new HashMap<>();
                for(int i = 0; i < params.length; i++)
                    p.put("p" + i, gson.toJson(params[i]));
                return p;
            }
        };
        request.setShouldCache(false);
        queue.add(request);
    }

    private void makePostRequest(final Uri url, final String message, final String errorMessage, final Object... objects) {
        LogManager.getInstance().printConsoleMessage("POST -> " + url.toString());
        StringRequest request = new StringRequest(Request.Method.POST, url.toString(),
                response -> {
                    if(response.equalsIgnoreCase("1"))
                        LogManager.getInstance().showVisualMessage(String.valueOf(R.string.update_done));
                    else
                        LogManager.getInstance().showVisualMessage(String.valueOf(R.string.update_failed));
                },
                error -> {
                    LogManager.getInstance().printConsoleError(error.networkResponse + "");
                    LogManager.getInstance().showVisualMessage(String.valueOf(R.string.update_failed));
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String>  params = new HashMap<>();
                for(int i = 0; i < objects.length; i++)
                    params.put("p" + i, gson.toJson(objects[i]));
                return params;
            }
        };
        request.setShouldCache(false);
        queue.add(request);
    }

    private void makeAdvancedGetRequest(final Uri url, final RecyclerView viewList, ArrayList list, Type type) {
        LogManager.getInstance().printConsoleMessage("GET -> " + url.toString());
        Response.Listener<String> listener = response -> {
                    list.clear();
                    list.addAll(gson.fromJson(response, type));
                    viewList.getAdapter().notifyDataSetChanged();

                    App.setAuxiliarViewsStatus(list.size() > 0 ? Constants.NO_VIEW_ACTIVE : Constants.EMPTY_VIEW_ACTIVE);
                };
        Response.ErrorListener errorListener = error -> LogManager.getInstance().printConsoleError("ERROR -> " + url.toString() + " || " + error.toString());
        makeGetRequest(url, listener, errorListener);
    }

    private void makeSimpleGetRequest(final Uri url, ArrayList list, Type type) {
        LogManager.getInstance().printConsoleMessage("GET -> " + url.toString());
        Response.Listener<String> listener = response -> {
            list.clear();
            list.addAll(gson.fromJson(response, type));
        };
        Response.ErrorListener errorListener = error -> LogManager.getInstance().printConsoleError("ERROR -> " + url.toString() + " || " + error.toString());
        makeGetRequest(url, listener, errorListener);
    }

    private void makeGetRequest(final Uri url, Response.Listener<String> listener, Response.ErrorListener errorListener){
        StringRequest request = new StringRequest(Request.Method.GET, url.toString(), listener, errorListener);
        request.setShouldCache(false);
        queue.add(request);
    }

    /* ------------------------------------------------------------------------------------------------------ */
    /* GET FUNCTIONS ---------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------ */

    public void downloadFeed(RecyclerView viewList) {
        String url = BASE_URL + URL_FEED;
        LogManager.getInstance().printConsoleMessage(user.getExams().size() + "");
        if (user.getExams().size() > 0){
            url = url + "?exams=" + user.getExams().get(0);
            for (int i = 1; i < user.getExams().size(); i++)
                url = url + "&exams=" + user.getExams().get(i);
        }

        makeAdvancedGetRequest(Uri.parse(url), viewList, feedElements, new TypeToken<ArrayList<Document>>() {}.getType());
    }

    public void downloadStarredDocs(RecyclerView list){
        String url = urlGenerator(BASE_URL + URL_STARRED, user.getUid(), "2");
        makeAdvancedGetRequest(Uri.parse(url), list, starredDocuments, new TypeToken<ArrayList<Document>>(){}.getType());
    }

    public void downloadStarredBooks(RecyclerView list){
        String url = urlGenerator(BASE_URL + URL_STARRED, user.getUid(), "1");
        makeAdvancedGetRequest(Uri.parse(url), list, starredBooks, new TypeToken<ArrayList<Document>>(){}.getType());
    }

    public void downloadAllExams(){
        String url = urlGenerator(BASE_URL + URL_EXAMS, user.getUniversity(), user.getCourse());
        makeSimpleGetRequest(Uri.parse(url), allExams, new TypeToken<ArrayList<String>>(){}.getType());
    }

    private void downloadAllUniversities(){
        String url = urlGenerator(BASE_URL + URL_UNIVERSITIES);
        makeSimpleGetRequest(Uri.parse(url), allUniversities, new TypeToken<ArrayList<String>>(){}.getType());
    }

    private void downloadAllCourses(){
        String url = urlGenerator(BASE_URL + URL_COURSES);
        makeSimpleGetRequest(Uri.parse(url), allCourses, new TypeToken<ArrayList<Course>>(){}.getType());
    }

    public void downloadUserProfile(String uid, MainActivity mainActivity){
        LogManager.getInstance().printConsoleMessage("downloadUserProfile");
        String url = urlGenerator(BASE_URL + URL_USER, uid);
        makeGetRequest(Uri.parse(url),
                response -> {
                    this.user = gson.fromJson(response, new TypeToken<User>(){}.getType());
                    LogManager.getInstance().printConsoleMessage(user.getExams().size() + "");
                    if(user != null) {
                        //downloadAllExams();
                        //downloadAllUniversities();
                        //downloadAllCourses();
                        mainActivity.startApp();
                    }
                    else
                        mainActivity.startLogin();
                },
                error -> mainActivity.recreate());
    }

    public void queryMaterials(String query, RecyclerView list){
        String url = urlGenerator(BASE_URL + URL_SEARCH, user.getUniversity(), query);
        makeAdvancedGetRequest(Uri.parse(url), list, searchElements, new TypeToken<ArrayList<Document>>(){}.getType());
    }

    public void downloadMyFiles(RecyclerView list) {
        String url = urlGenerator(BASE_URL + URL_SEARCH, user.getUid());
        makeAdvancedGetRequest(Uri.parse(url), list, myFiles, new TypeToken<ArrayList<Document>>(){}.getType());
    }

    public void downloadMyChats(RecyclerView list) {
        String url = urlGenerator(BASE_URL + URL_CHATS, user.getUid());
        makeAdvancedGetRequest(Uri.parse(url), list, allChats, new TypeToken<ArrayList<Chat>>(){}.getType());
    }

    public void downloadCurrentActivePositions(Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String url = urlGenerator(BASE_URL + URL_POSITION, user.getUid());
        makeGetRequest(Uri.parse(url), listener, errorListener);
    }

    public void checkIfTheUserAlreadyExists(Response.Listener<String> listener, Response.ErrorListener errorListener, String email){
        String url = urlGenerator(BASE_URL + URL_CHECK, email);
        makeGetRequest(Uri.parse(url), listener, errorListener);
    }

    public void createValidationProviderRequest(Response.Listener<String> listener, Response.ErrorListener errorListener, String email, int method){
        String url = urlGenerator(BASE_URL + URL_PROVIDER_VALIDATION, email, String.valueOf(method));
        makeGetRequest(Uri.parse(url), listener, errorListener);
    }

    /* ------------------------------------------------------------------------------------------------------ */
    /* POST FUNCTIONS --------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------ */

    private void uploadUser(){
        Uri url = Uri.parse(BASE_URL + URL_USER);
        makePostRequest(url,
                App.getStringFromRes(R.string.upload_user_message),
                App.getStringFromRes(R.string.upload_user_error),
                user);
    }

    public void uploadDocument(final Document document){
        Uri url = Uri.parse(BASE_URL + URL_DOC);
        makePostRequest(url,
                App.getStringFromRes(!document.getModified() ? R.string.upload_doc_message : R.string.upload_doc_message2),
                App.getStringFromRes(!document.getModified() ? R.string.upload_doc_error : R.string.upload_doc_error2),
                document);
    }

    public void uploadPosition(Position position){
        Uri url = Uri.parse(BASE_URL + URL_POSITION);
        makePostRequest(url,
                App.getStringFromRes(R.string.upload_pos_message),
                App.getStringFromRes(R.string.upload_pos_error),
                position);
    }

    public void uploadMessage(Chat chat, Message message) {
        Uri url = Uri.parse(BASE_URL + URL_MESSAGE);
        makePostRequest(url, null,
                App.getStringFromRes(R.string.upload_mess_error),
                chat,
                message);
    }

    public void createNewUser(final User user, Response.Listener<String> listener, Response.ErrorListener errorListener){
        String url = BASE_URL + URL_USER;
        makeCustomPostRequest(Uri.parse(url), listener, errorListener, user);
    }

    public void postToken(final String uid, final String token, Response.Listener<String> listener, Response.ErrorListener errorListener){
        String url = BASE_URL + URL_TOKEN;
        makeCustomPostRequest(Uri.parse(url), listener, errorListener, uid, token);
    }

    public void deletePosition(){
        String url = BASE_URL + URL_POS_REMOVE;
        makeCustomPostRequest(Uri.parse(url), response -> {}, error -> {}, this.getUser().getUid());

    }

    /* ------------------------------------------------------------------------------------------------------ */
    /* STORAGE FUNCTIONS ------------------------------------------------------------------------------------ */
    /* ------------------------------------------------------------------------------------------------------ */

    private void uploadProfileImageOnServer(Bitmap bitmap) {
        LogManager.getInstance().printConsoleMessage("Upload -> image");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_PROFILE_IMAGES + user.getUid());
        mStorage.putBytes(baos.toByteArray()).addOnSuccessListener(taskSnapshot -> mStorage.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    user.setPhotosUrl(uri.toString());
                    updateUser(null);
                }))
                .addOnFailureListener(e -> LogManager.getInstance().showVisualError(e, App.getStringFromRes(R.string.upload_user_error)));
    }

    public void uploadFileOnTheServer(String url, Document document){
        LogManager.getInstance().printConsoleMessage("Upload -> file");
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PATH_UPLOADS + DataManager.getInstance().getMiniUser().getName() + "_" + document.getTitle().toLowerCase() + ".pdf");
        UploadTask task = reference.putFile(Uri.parse(url));

        task.addOnSuccessListener(taskSnapshot -> {
            document.setUrl(taskSnapshot.getUploadSessionUri().toString());
            uploadDocument(document);
        }).addOnFailureListener(e -> LogManager.getInstance().printConsoleMessage(e.toString()));
    }

    public ArrayList<Chat> getAllChats() {
        return allChats;
    }
}