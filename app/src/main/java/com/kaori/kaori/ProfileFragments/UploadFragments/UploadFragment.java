package com.kaori.kaori.ProfileFragments.UploadFragments;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaori.kaori.Model.Document;
import com.kaori.kaori.R;

import java.util.List;

public class UploadFragment extends Fragment {

    /**
     * Constants
     */
    private final String BACK_STATE_NAME = getClass().getName();
    private final static int PICK_PDF_CODE = 2342;
    private final String GS_URL = "gs://kaori-c5a43.appspot.com";

    /**
     * Views from layout
     */
    private EditText mTitle, mAuthor, mComment, mLink, mStatus;
    private StorageReference storage;
    private String tag;
    private Document newMaterial;
    private RadioGroup radioGroup;
    private View view;
    private List<String> exams;
    private List<CheckBox> checkBoxes;
    private boolean isMaterialModified;
    private final String[] titles = { "book", "document", "url" };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upload_layout, container, false);
        ViewPager viewPager = view.findViewById(R.id.viewpager);

        TabLayout tabLayout = view.findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getChildFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(viewPageAdapter);

        storage = FirebaseStorage.getInstance().getReferenceFromUrl(GS_URL);
        return view;
    }

    private class ViewPageAdapter extends FragmentPagerAdapter {

        private int numOfTabs;

        /*package-private*/ ViewPageAdapter(FragmentManager fm, int numOfTabs) {
            super(fm);
            this.numOfTabs = numOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new UploadBookFragment();
                case 1:
                    return new UploadDocumentFragment();
                case 2:
                    return new UploadUrlFragment();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return numOfTabs;
        }

    }

    /**
     * This method calls the wait fragment to manage the upload of
     * the material in Firebase Firestore and Storage
     */
    private void endProcess(){
        UploadWaitFragment uploadWaitFragment = new UploadWaitFragment();
        uploadWaitFragment.setParameters(tag, newMaterial);

        if(getActivity() != null && getActivity().getSupportFragmentManager() != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, uploadWaitFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(BACK_STATE_NAME)
                    .commit();
    }



}