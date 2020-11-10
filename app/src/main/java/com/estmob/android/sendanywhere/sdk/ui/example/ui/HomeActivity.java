package com.estmob.android.sendanywhere.sdk.ui.example.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.estmob.android.sendanywhere.sdk.ui.example.Constant;
import com.estmob.android.sendanywhere.sdk.ui.example.PreferenceHelper;
import com.estmob.android.sendanywhere.sdk.ui.example.R;
import com.estmob.android.sendanywhere.sdk.ui.example.common.BaseActivity;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ApMgr;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.FileUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.TextUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ToastUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.view.MyScrollView;
import com.estmob.android.sendanywhere.sdk.ui.example.utils.CustomTypefaceSpan;
import com.estmob.android.sendanywhere.sdk.ui.example.utils.NavigatorUtils;
import com.google.android.material.navigation.NavigationView;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mopub.common.logging.MoPubLog.LogLevel.INFO;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();


    /**
     * Two large pieces of UI on the left and right
     */
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    TextView tv_name;

    /**
     * top bar related UI
     */
   /* @BindView(R.id.ll_mini_main)
    LinearLayout ll_mini_main;*/
  /*  @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.iv_mini_avator)*/
   /* ImageView iv_mini_avator;
    @BindView(R.id.btn_send)
    Button btn_send;
    @BindView(R.id.btn_receive)
    Button btn_receive;*/

    /**
     * Other UI
     */
    //@BindView(R.id.msv_content)
    //  MyScrollView mScrollView;
    /*@BindView(R.id.ll_main)
    LinearLayout ll_main;*/
    @BindView(R.id.btn_send_big)
    ImageView btn_send_big;
    @BindView(R.id.btn_receive_big)
    ImageView btn_receive_big;

    @BindView(R.id.btn_sidemenu)
    ImageView btnSidemenu;

    @BindView(R.id.btn_history)
    ImageView btnHistory;
   /* @BindView(R.id.rl_device)
    RelativeLayout rl_device;
    @BindView(R.id.tv_device_desc)
    TextView tv_device_desc;
    @BindView(R.id.rl_file)
    RelativeLayout rl_file;
    @BindView(R.id.tv_file_desc)
    TextView tv_file_desc;
    @BindView(R.id.rl_storage)
    RelativeLayout rl_storage;
    @BindView(R.id.tv_storage_desc)
    TextView tv_storage_desc;*/

    //大的我要发送和我要接受按钮的LinearLayout的高度
    boolean issender;
    boolean isreceiver;

    //
    boolean mIsExist = false;
    Handler mHandler = new Handler();

    private int PERMISSION_REQUEST_CODE = 1;
    private int PERMISSION_REQUEST_CODE_WRITE = 2;
    private int PERMISSION_REQUEST_CODE_HOTSPOT = 7;
    private static final int ACTION_LOCATION_SOURCE_SETTINGS = 3;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        ButterKnife.bind(this);
        //Android6.0 requires android.permission.READ_EXTERNAL_STORAGE
        //TODO
        prefs = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
        init();

        btnSidemenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerLayout != null) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
        final SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder("12345123451234512345123451234511");

        configBuilder.withLogLevel(INFO);
        MoPub.initializeSdk(this, configBuilder.build(), initSdkListener());
        btn_send_big.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                issender = true;
                isreceiver = false;
                ArrayList<String> permissionRequires = checkAndRequestPermissions();
                if (permissionRequires.size() == 0) {
                    if (!isLocationEnabled()) {
                        requestLocationPermissionDialog();
                    } else {
                        if (!hasWritePermission()) {
                            requestWritePermission();
                        } else {
                            scanHotSpots();
                            //NavigatorUtils.toChooseFileUI(getContext());
                        }
                    }
                } else {
                    requestPermission(permissionRequires);
                }
            }
        });

        btn_receive_big.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                issender = false;
                isreceiver = true;
                if (checkAndRequestPermissions().size() == 0) {
                    if (!locationEnabled(HomeActivity.this)) {
                        requestLocationPermissionDialog();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getApplicationContext())) {
                            requestWritePermission();
                        } else {
                            scanHotSpots();
                        }
                    }
                } else {
                    requestPermission(checkAndRequestPermissions());
                }
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu m = mNavigationView.getMenu();
        for (int i=0;i<m.size();i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu!=null && subMenu.size() >0 ) {
                for (int j=0; j <subMenu.size();j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
        mNavigationView.setNavigationItemSelectedListener(this);

        //设置设备名称
        String device = TextUtils.isNullOrBlank(android.os.Build.DEVICE) ? Constant.DEFAULT_SSID : android.os.Build.DEVICE;
        try {//设置左边抽屉的设备名称
            tv_name = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.tv_name);
            tv_name.setText(device);
        } catch (Exception e) {
            //maybe occur some exception
        }

        //  mScrollView.setOnScrollListener(this);

        //ll_mini_main.setClickable(false);
        // ll_mini_main.setVisibility(View.GONE);

        //updateBottomData();
    }

    private void updateBottomData() {
        //TODO 设备数的更新
        //TODO 文件数的更新
        // tv_file_desc.setText(String.valueOf(FileUtils.getReceiveFileCount()));
        //TODO 节省流量数的更新
        // tv_storage_desc.setText(String.valueOf(FileUtils.getReceiveFileListTotalLength()));

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
//                super.onBackPressed();
                if (mIsExist) {
                    this.finish();
                } else {
                    ToastUtils.show(getContext(), getContext().getResources().getString(R.string.tip_call_back_agin_and_exist)
                            .replace("{appName}", getContext().getResources().getString(R.string.app_name)));
                    mIsExist = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsExist = false;
                        }
                    }, 2 * 1000);

                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chng_folder) {
        } else if (id == R.id.nav_share_his) {
        } else if (id == R.id.nav_invite_frnd) {
        } else if (id == R.id.nav_app_lang) {
        } else if (id == R.id.nav_privacy) {
        } else if (id == R.id.nav_about_us) {
        } else if (id == R.id.nav_rate_now) {
        } else {
            ToastUtils.show(getContext(), getResources().getString(R.string.tip_next_version_update));
        }

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "LitSans-Medium.otf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("" , font), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    /*  @OnClick({R.id.btn_send, R.id.btn_receive, R.id.btn_send_big, R.id.btn_receive_big, R.id.iv_mini_avator
     *//*R.id.rl_device, R.id.rl_file, R.id.rl_storage*//*})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
            case R.id.btn_send_big: {

                break;
            }
            case R.id.btn_receive:
            case R.id.btn_receive_big: {

                break;
            }
            case R.id.iv_mini_avator: {

                break;
            }
           *//* case R.id.rl_file:
            case R.id.rl_storage: {
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
               // NavigatorUtils.toSystemFileChooser(getContext());
                break;*//*
        }

    }*/

   /* private void toProject() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(Constant.GITHUB_PROJECT_SITE);
        intent.setData(uri);
        getContext().startActivity(intent);
    }
*/

    public static boolean locationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return isGpsEnabled || isNetworkEnabled;
        } else {
            return false;
        }
    }

    public boolean isLocationEnabled() {
        return locationEnabled(HomeActivity.this);
    }

    public void requestWritePermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setCancelable(false);
        builder.setTitle(HomeActivity.this.getResources().getString(R.string.str_require_permission));
        builder.setMessage(HomeActivity.this.getResources().getString(R.string.str_enable_write));
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSION_REQUEST_CODE_WRITE);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // resetEverything("Write permission no button");
            dialog.cancel();
        });
        builder.show();

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermission(ArrayList<String> listPermissionsNeeded) {
        String[] array = new String[listPermissionsNeeded.size()];

        for (int i = 0; i < listPermissionsNeeded.size(); i++) {
            array[i] = listPermissionsNeeded.get(i);
        }
        if (listPermissionsNeeded.size() > 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    HomeActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)
            ) {
                prefs.edit().putBoolean(PreferenceHelper.PermissionAsked, true).apply();
                requestPermissions(
                        array,
                        PERMISSION_REQUEST_CODE
                );
            } else {
                if (prefs.getBoolean(PreferenceHelper.PermissionAsked, false)) {
                    requestPermissionDialog();
                } else {
                    prefs.edit().putBoolean(PreferenceHelper.PermissionAsked, true).apply();
                    requestPermissions(
                            array,
                            PERMISSION_REQUEST_CODE
                    );
                }
            }
        }
    }

    public void requestLocationPermissionDialog() {
        new AlertDialog.Builder(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(R.string.title_location_services)
                .setCancelable(false)
                .setMessage(R.string.message_location_services)
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> {
                    //resetEverything("location cancel");
                })
                .setPositiveButton(R.string.action_enable, (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, ACTION_LOCATION_SOURCE_SETTINGS);
                })
                .show();
    }

    public boolean hasWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(HomeActivity.this.getApplicationContext());
        }
        return true;
    }

    public ArrayList<String> checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT <= 21) {
            return new ArrayList<>();
        }
        int permissionCamera = ContextCompat.checkSelfPermission(
                HomeActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION
        );
        int storage = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readStorage = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        ArrayList<String> listPermissionsNeeded = new ArrayList<>();


        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (readStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        return listPermissionsNeeded;
    }

    private void requestPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setCancelable(false);
        builder.setTitle("Required Permission");
        builder.setMessage("Please enable the permissions in order to connect to device");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", HomeActivity.this.getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            //resetEverything("QR code no button");
            dialog.cancel();
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE || requestCode == PERMISSION_REQUEST_CODE_WRITE || requestCode == ACTION_LOCATION_SOURCE_SETTINGS || requestCode == PERMISSION_REQUEST_CODE_HOTSPOT) {
            if (checkAndRequestPermissions().size() == 0) {
                if (!locationEnabled(HomeActivity.this)) {
                    requestLocationPermissionDialog();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getApplicationContext())) {
                        requestWritePermission();
                    } else {
                        scanHotSpots();
                    }
                }
            }
        } else {
            if (resultCode != Activity.RESULT_OK) {
                if (data == null)
                    return;
                //Getting the passed result
                String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
                if (result != null) {
                    AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert).create();
                    alertDialog.setTitle("Scan Error");
                    alertDialog.setMessage("QR Code could not be scanned");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                }
                return;

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkAndRequestPermissions().size() == 0) {
            if (!locationEnabled(HomeActivity.this)) {
                requestLocationPermissionDialog();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getApplicationContext())) {
                    requestWritePermission();
                } else {
                    scanHotSpots();
                }
            }
        } else {
            //resetEverything("Permission denied");
        }
    }

    private void scanHotSpots() {
        if (ApMgr.isApOn(getContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestHotSpotDisable();
            } else {
                ApMgr.disableAp(HomeActivity.this);
            }
        } else {
            if (issender) {
                NavigatorUtils.toChooseFileUI(getContext());
            } else if (isreceiver) {
                NavigatorUtils.toReceiverWaitingUI(getContext());
            }
        }

    }

    private void requestHotSpotDisable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setCancelable(false);
        builder.setTitle(HomeActivity.this.getResources().getString(R.string.str_turn_off_hotspot));
        builder.setMessage(HomeActivity.this.getResources().getString(R.string.str_turn_off_wifi));
        builder.setPositiveButton("Yes", (dialog, which) -> {
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
            intent.setComponent(cn);
            startActivityForResult(intent, PERMISSION_REQUEST_CODE_HOTSPOT);
            dialog.dismiss();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            //resetEverything("No button");
            dialog.cancel();
        });
        builder.show();
    }
}
