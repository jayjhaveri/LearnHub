package com.jayjhaveri.learnhub;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.jayjhaveri.learnhub.Fragments.MostPopularFragment;
import com.jayjhaveri.learnhub.Fragments.MostRecentFragment;

public class CategoryActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public void loadCategoryList() {

    }

    @Override
    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MostPopularFragment(), getString(R.string.most_popular_fragment_title));
        adapter.addFragment(new MostRecentFragment(), getString(R.string.most_recent_fragment_title));
        viewPager.setAdapter(adapter);
    }
}
