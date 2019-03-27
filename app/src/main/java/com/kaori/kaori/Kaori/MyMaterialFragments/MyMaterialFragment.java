package com.kaori.kaori.Kaori.MyMaterialFragments;

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

import com.kaori.kaori.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Search Fragment is used to search materials.
 */
public class MyMaterialFragment extends Fragment {

    /**
     * Constants.
     */
    private final String[] titles = { "docs", "books" };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mymaterial_layout, container, false);
        ViewPager viewPager = view.findViewById(R.id.viewpager);

        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getChildFragmentManager());
        viewPageAdapter.addFragment(new MyDocsFragment(), "Docs");
        viewPageAdapter.addFragment(new MyBooksFragment(), "Books");
        viewPager.setAdapter(viewPageAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    private class ViewPageAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPageAdapter(FragmentManager childFragmentManager) {
            super(childFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

}
