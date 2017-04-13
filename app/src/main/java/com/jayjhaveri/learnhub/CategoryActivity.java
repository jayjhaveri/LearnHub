package com.jayjhaveri.learnhub;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.jayjhaveri.learnhub.Fragments.MostPopularFragment;
import com.jayjhaveri.learnhub.Fragments.MostRecentFragment;
import com.jayjhaveri.learnhub.adapter.ViewPagerAdapter;

public class CategoryActivity extends MainActivity {

    public static String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        categoryName = getIntent().getStringExtra(getString(R.string.category_name));

        getSupportActionBar().setTitle(categoryName);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadAuthNavigationDrawer(toolbar);
        loadWithoutAuthNavigationDrawer(toolbar);
    }




    @Override
    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MostPopularFragment(), getString(R.string.most_popular_fragment_title));
        adapter.addFragment(new MostRecentFragment(), getString(R.string.most_recent_fragment_title));
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d("category", "" + item.getItemId());
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void loadAuthNavigationDrawer(Toolbar toolbar) {

    }

    @Override
    protected void loadWithoutAuthNavigationDrawer(Toolbar toolbar) {

    }

}
