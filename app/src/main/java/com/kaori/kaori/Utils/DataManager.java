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
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kaori.kaori.Kaori;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
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

    /**
     * TODO: da mettere in strings.xml
     * Success messages.
     */
    private final String feedMessage = "Download feed completato.";

    /**
     * TODO: da mettere in strings.xml
     * Error messages.
     */
    private final String feedError = "Download feed fallito";

    /**
     * Singleton instance.
     */
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

    private DataManager(Context context) {
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

    public void clean(Context context) {
        dataManager = new DataManager(context);
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
        LogManager.getInstance().printConsoleMessage("Post -> " + url.toString());
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

    private void makePostRequest(final Uri url, final Object... objects) {
        LogManager.getInstance().printConsoleMessage("Post -> " + url.toString());
        StringRequest request = new StringRequest(Request.Method.POST, url.toString(),
                response -> {
                    if(response.equalsIgnoreCase("1"))
                        LogManager.getInstance().showVisualMessage("Aggiornamento effettuato.");
                    else
                        LogManager.getInstance().showVisualMessage("Aggiornamento fallito, riprovare.");
                },
                error -> {
                    LogManager.getInstance().printConsoleError(error.networkResponse + "");
                    LogManager.getInstance().showVisualMessage("Aggiornamento fallito, riprovare.");
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

    private void makeAdvancedGetRequest(final Uri url, final RecyclerView viewList, View view, ArrayList list, Type type) {
        LogManager.getInstance().printConsoleMessage("Get -> " + url.toString());
        Response.Listener<String> listener = response -> {
                    list.clear();
                    list.addAll(gson.fromJson(response, type));

                    LogManager.getInstance().printConsoleMessage(response);

                    viewList.getAdapter().notifyDataSetChanged();
                    view.findViewById(R.id.wait_layout).setVisibility(View.GONE);

                    if (list.size() == 0) {
                        //TODO: personalizzare
                        ((TextView) view.findViewById(R.id.empty_view_text)).setText(R.string.feed_empty_view_text);
                        view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                    }

                    LogManager.getInstance().printConsoleMessage("Completed.");
                };
        Response.ErrorListener errorListener = error -> LogManager.getInstance().printConsoleError("Feed: " + error.toString() + " " + error.networkResponse.statusCode);
        makeGetRequest(url, listener, errorListener);
    }

    private void makeSimpleGetRequest(final Uri url, ArrayList list, Type type) {
        LogManager.getInstance().printConsoleMessage("Get -> " + url.toString());
        Response.Listener<String> listener = response -> {
            list.clear();
            list.addAll(gson.fromJson(response, type));
            LogManager.getInstance().printConsoleMessage("Completed.");
        };
        Response.ErrorListener errorListener = error -> { LogManager.getInstance().printConsoleError("Error."); };
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

    public void downloadFeed(RecyclerView list, View view) {
        LogManager.getInstance().printConsoleMessage("downloadFeed");
        String url = BASE_URL + URL_FEED;
        if (user.getExams().size() > 0){
            url = url + "?exams=" + user.getExams().get(0);
            for (int i = 1; i < user.getExams().size(); i++)
                url = url + "&exams=" + user.getExams().get(i);
        }

        makeAdvancedGetRequest(Uri.parse(url), list, view, feedElements, new TypeToken<ArrayList<Document>>() {}.getType());
    }

    public void downloadStarredDocs(RecyclerView list, View view){
        String url = urlGenerator(BASE_URL + URL_STARRED, user.getUid(), "2");
        makeAdvancedGetRequest(Uri.parse(url), list, view, starredDocuments, new TypeToken<ArrayList<Document>>(){}.getType());
    }

    public void downloadStarredBooks(RecyclerView list, View view){
        String url = urlGenerator(BASE_URL + URL_STARRED, user.getUid(), "1");
        makeAdvancedGetRequest(Uri.parse(url), list, view, starredBooks, new TypeToken<ArrayList<Document>>(){}.getType());
    }

    private void downloadAllExams(){
        LogManager.getInstance().printConsoleError("downloadAllExams");
        String url = urlGenerator(BASE_URL + URL_EXAMS, user.getUniversity(), user.getCourse());
        makeSimpleGetRequest(Uri.parse(url), allExams, new TypeToken<ArrayList<String>>(){}.getType());
    }

    private void downloadAllUniversities(){
        LogManager.getInstance().printConsoleError("downloadAllUniversities");
        String url = urlGenerator(BASE_URL + URL_UNIVERSITIES);
        makeSimpleGetRequest(Uri.parse(url), allUniversities, new TypeToken<ArrayList<String>>(){}.getType());
    }

    private void downloadAllCourses(){
        LogManager.getInstance().printConsoleError("downloadAllCourses");
        String url = urlGenerator(BASE_URL + URL_COURSES);
        makeSimpleGetRequest(Uri.parse(url), allCourses, new TypeToken<ArrayList<Course>>(){}.getType());
    }

    public void downloadUserProfile(String uid, Kaori kaori){
        LogManager.getInstance().printConsoleError("downloadUserProfile");
        String url = urlGenerator(BASE_URL + URL_USER, uid);
        makeGetRequest(Uri.parse(url),
                response -> {
                    user = gson.fromJson(response, new TypeToken<User>(){}.getType());
                    if(user != null) {
                        downloadAllExams();
                        downloadAllUniversities();
                        downloadAllCourses();
                        LogManager.getInstance().printConsoleMessage("Completed.");
                        kaori.startApp();
                    }
                    else {
                        LogManager.getInstance().printConsoleError("Profile not loaded.");
                        kaori.startLogin();
                    }
                },
                error -> {
                    LogManager.getInstance().printConsoleError(error.toString());
                    FirebaseAuth.getInstance().signOut();
                    kaori.startLogin();
                });
    }

    public void queryMaterials(String query, RecyclerView list, View view){
        String url = urlGenerator(BASE_URL + URL_SEARCH, user.getUniversity(), query);
        makeAdvancedGetRequest(Uri.parse(url), list, view, searchElements, new TypeToken<ArrayList<Document>>(){}.getType());
    }

    public void downloadMyFiles(RecyclerView list, View view) {
        String url = urlGenerator(BASE_URL + URL_SEARCH, user.getUid());
        makeAdvancedGetRequest(Uri.parse(url), list, view, myFiles, new TypeToken<ArrayList<Document>>(){}.getType());
    }

    public void downloadMyChats(RecyclerView list, View view) {
        String url = urlGenerator(BASE_URL + URL_CHATS, user.getUid());
        makeAdvancedGetRequest(Uri.parse(url), list, view, allChats, new TypeToken<ArrayList<Chat>>(){}.getType());
    }

    public void downloadCurrentActivePositions(RecyclerView list, View view) {
        String url = urlGenerator(BASE_URL + URL_POSITION, user.getUid());
        Response.Listener<String> response = response1 -> {
            currentActivePositions.clear();
            currentActivePositions.addAll(gson.fromJson(response1, new TypeToken<ArrayList<Position>>(){}.getType()));

            // today date at 00:00:00
            Calendar date = new GregorianCalendar();
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);

            // remove old positions
            for (Position p : currentActivePositions) {
                Calendar mDate = new GregorianCalendar();
                mDate.setTimeInMillis(p.getTimestamp()*1000);
                if (mDate.before(date.getTime()))
                    currentActivePositions.remove(p);
            }
            view.findViewById(R.id.wait_layout).setVisibility(View.GONE);
            list.getAdapter().notifyDataSetChanged();

            if (currentActivePositions.size() == 0) {
                ((TextView) view.findViewById(R.id.empty_view_text)).setText(R.string.feed_empty_view_text);
                view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            }

            LogManager.getInstance().printConsoleMessage("Completed.");
        };
        makeGetRequest(Uri.parse(url), response, error -> {});
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
        makePostRequest(url, user);
    }

    public void uploadDocument(Document document){
        Uri url = Uri.parse(BASE_URL + URL_DOC);
        makePostRequest(url, document);
    }

    public void uploadPosition(Position position){
        Uri url = Uri.parse(BASE_URL + URL_POSITION);
        makePostRequest(url, position);
    }

    public void uploadMessage(Chat chat, Message message) {
        Uri url = Uri.parse(BASE_URL + URL_MESSAGE);
        makePostRequest(url, chat, message);
    }

    public void createNewUser(final User user, Response.Listener<String> listener, Response.ErrorListener errorListener){
        String url = BASE_URL + URL_USER;
        makeCustomPostRequest(Uri.parse(url), listener, errorListener, user);
    }

    public void postToken(final String uid, final String token, Response.Listener<String> listener, Response.ErrorListener errorListener){
        String url = BASE_URL + URL_TOKEN;
        makeCustomPostRequest(Uri.parse(url), listener, errorListener, uid, token);
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
                    LogManager.getInstance().printConsoleMessage("Il tuo profilo è stato aggiornato.");
                    user.setPhotosUrl(uri.toString());
                    updateUser(null);
                }))
                .addOnFailureListener(e -> LogManager.getInstance().showVisualError(e, "Il tuo profilo non è stato aggiornato."));
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