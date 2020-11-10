package com.estmob.android.sendanywhere.sdk.ui.example.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.estmob.android.sendanywhere.sdk.ui.example.R;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.fragment.HistoryApkFragment;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.fragment.HistoryAudioFragment;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.fragment.HistoryImageFragment;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.fragment.HistoryVideoFragment;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends AppCompatActivity {

    @BindView(R.id.tab_layout)
    TabLayout tab_layout;
    @BindView(R.id.view_pager)
    ViewPager view_pager;
    ResPagerAdapter adapter;

    @BindView(R.id.btn_back)
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        ButterKnife.bind(this);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
       initData();
    }

    private void initData() {
        String[] titles = getResources().getStringArray(R.array.array_res);
        adapter =  new ResPagerAdapter(getSupportFragmentManager(), titles);
        view_pager.setAdapter(adapter);
        view_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
//                <item>应用</item>
//                <item>图片</item>
//                <item>音乐</item>
//                <item>视频</item>
                if (position == 0) { //应用

                } else if (position == 1) { //图片

                } else if (position == 2) { //音乐

                } else if (position == 3) { //视频

                } else if (position == 4) {

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        view_pager.setOffscreenPageLimit(4);
        tab_layout.setTabMode(TabLayout.MODE_FIXED);
        tab_layout.setupWithViewPager(view_pager);

    }

    class ResPagerAdapter extends FragmentPagerAdapter {
        String[] sTitleArray;

        public ResPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public ResPagerAdapter(FragmentManager fm, String[] sTitleArray) {
            this(fm);
            this.sTitleArray = sTitleArray;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) { //图片
                fragment = HistoryApkFragment.newInstance();
            } else if (position == 1) { //音乐
                fragment = HistoryImageFragment.newInstance();
            } else if (position == 2) { //视频
                fragment = HistoryAudioFragment.newInstance();
            } else if (position == 3) {
                fragment = HistoryVideoFragment.newInstance();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return sTitleArray.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return sTitleArray[position];
        }
    }
}
