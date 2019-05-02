package cn.bavelee.pokeinstaller;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.runtimepermission.RuntimePermission;
import com.github.florent37.runtimepermission.callbacks.PermissionListener;

import java.util.List;

import cn.bavelee.pokeinstaller.apk.APKCommander;
import cn.bavelee.pokeinstaller.apk.ApkInfo;
import cn.bavelee.pokeinstaller.apk.ICommanderCallback;

public class PokeInstallerActivity extends FragmentActivity implements ICommanderCallback, View.OnClickListener {

    private TextView tvAppName;
    private LinearLayout layoutAppDetails;
    private ImageView imgAppIcon;

    private ProgressBar progressBar;
    private LinearLayout layoutTitleContainer;
    private LinearLayout layoutPermissionList;
    private LinearLayout layoutButtons;

    private TextView btnInstall;
    private TextView btnSilently;
    private TextView btnCancel;


    private APKCommander apkCommander;

    private int[] settingsColors = new int[4];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_poke_installer);
        layoutAppDetails = findViewById(R.id.layout_app_details);
        tvAppName = findViewById(R.id.tv_app_name);
        layoutTitleContainer = findViewById(R.id.titleBar);
        imgAppIcon = findViewById(R.id.img_app_icon);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        btnInstall = findViewById(R.id.btn_install);
        btnSilently = findViewById(R.id.btn_silently);
        btnCancel = findViewById(R.id.btn_cancel);
        layoutButtons = (LinearLayout) btnInstall.getParent();
        btnInstall.setEnabled(true);
        btnInstall.setOnClickListener(this);
        btnSilently.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        loadSettings();
        if (getIntent().getData() == null) {
            finish();
        } else
            checkPermission();
    }

    private void loadSettings() {
        findViewById(R.id.ic_settings).setAlpha(Prefs.getPreference(this).getBoolean("show_settings", true) ? 1 : 0);
        findViewById(R.id.ic_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PokeInstallerActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 100);
            }
        });

        settingsColors[0] = getColor("progress_normal_color");
        settingsColors[1] = getColor("progress_installing_color");
        settingsColors[2] = getColor("progress_success_color");
        settingsColors[3] = getColor("progress_failure_color");
        LinearLayout.LayoutParams marginParams = (LinearLayout.LayoutParams) btnInstall.getLayoutParams();
        marginParams.setMargins(Integer.parseInt(Prefs.getPreference(this).getString("btn_margins", "0")), 0, 0, 0);
        btnInstall.setLayoutParams(marginParams);
        btnSilently.setLayoutParams(marginParams);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor("status_bar_color"));
        layoutTitleContainer.setBackgroundColor(settingsColors[0]);
        if (apkCommander != null && apkCommander.getApkInfo() != null && apkCommander.getApkInfo().getApkFile() != null) {
            initDetails(apkCommander.getApkInfo());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadSettings();
    }

    private void checkPermission() {
        RuntimePermission.askPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .ask(new PermissionListener() {
                    @Override
                    public void onAccepted(RuntimePermission runtimePermission, List<String> accepted) {
                        apkCommander = new APKCommander(PokeInstallerActivity.this, getIntent().getData(), PokeInstallerActivity.this);
                    }

                    @Override
                    public void onDenied(RuntimePermission runtimePermission, List<String> denied, List<String> foreverDenied) {
                        Toast.makeText(PokeInstallerActivity.this, R.string.no_permissions, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private int getColor(String key) {
        return Prefs.getPreference(this).getInt(key, getResources().getColor(R.color.colorPrimary));
    }

    private void initDetails(ApkInfo apkInfo) {
        layoutAppDetails.removeAllViews();
        tvAppName.setText(apkInfo.getAppName());
        imgAppIcon.setImageDrawable(apkInfo.getIcon());
        layoutAppDetails.addView(createAppInfoView(getString(R.string.info_pkg_name), apkInfo.getPackageName()));
        layoutAppDetails.addView(createAppInfoView(getString(R.string.info_apk_path), apkInfo.getApkFile().getPath()));
        layoutAppDetails.addView(createAppInfoView(getString(R.string.info_version), apkInfo.getVersion()));
        if (apkInfo.hasInstalledApp())
            layoutAppDetails.addView(createAppInfoView(getString(R.string.info_installed_version), apkInfo.getInstalledVersion()));
        if (Prefs.getPreference(this).getBoolean("show_perm", true)) {
            if (apkInfo.getPermissions() != null && apkInfo.getPermissions().length > 0) {
                layoutPermissionList = new LinearLayout(this);
                layoutPermissionList.setOrientation(LinearLayout.VERTICAL);
                layoutPermissionList.addView(createAppInfoView(null, getString(R.string.app_permissions)));
                for (String perm : apkInfo.getPermissions()) {
                    layoutPermissionList.addView(createAppPermissionView(perm));
                }
                layoutAppDetails.addView(layoutPermissionList);
            }
        }
    }

    private LinearLayout createAppPermissionView(String perm) {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.info_item_permission, null, false);
        TextView tv1 = (TextView) layout.getChildAt(0);
        tv1.setText(perm);
        tv1.setTextColor(getColor("perm_color"));
        return layout;
    }


    private LinearLayout createAppInfoView(String key, String value) {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.info_item, null, false);
        TextView tv1 = (TextView) layout.getChildAt(0);
        TextView tv2 = (TextView) layout.getChildAt(1);
        tv1.setText(key);
        tv2.setText(value);
        if (TextUtils.isEmpty(value)) {
            layout.removeView(tv2);
            tv1.setTypeface(Typeface.MONOSPACE);
            tv1.setGravity(Gravity.START);
        }
        if (TextUtils.isEmpty(key)) {
            layout.removeView(tv2);
            tv1.setText(value);
        }
        return layout;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apkCommander.getApkInfo() != null && apkCommander.getApkInfo().isFakePath())
            apkCommander.getApkInfo().getApkFile().delete();
    }


    @Override
    public void onStartParseApk(Uri uri) {
        TextView textView = new TextView(this);
        textView.setTextColor(Color.RED);
        textView.setText(getString(R.string.parsing) + " : " + uri.toString());
        layoutAppDetails.addView(textView);
        btnInstall.setVisibility(View.GONE);
    }

    @Override
    public void onApkParsed(ApkInfo apkInfo) {
        if (apkInfo != null && !TextUtils.isEmpty(apkInfo.getPackageName())) {
            initDetails(apkInfo);
            btnInstall.setVisibility(View.VISIBLE);
        } else {
            Uri uri = getIntent().getData();
            String s = null;
            if (uri != null)
                s = uri.toString();
            TextView textView = new TextView(this);
            textView.setTextColor(Color.RED);
            textView.setText(getString(R.string.parse_apk_failed, s));
            layoutAppDetails.addView(textView);
        }
    }

    @Override
    public void onApkPreInstall(ApkInfo apkInfo) {
        if (layoutPermissionList != null)
            layoutAppDetails.removeView(layoutPermissionList);
        tvAppName.setText(R.string.installing);
        btnInstall.setEnabled(false);
        btnSilently.setEnabled(false);
        progressBar.setVisibility(
                Prefs.getPreference(this).getBoolean("show_progress_bar", true) ?
                        View.VISIBLE : View.INVISIBLE);
        layoutTitleContainer.setBackgroundColor(settingsColors[1]);
        layoutButtons.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onApkInstalled(ApkInfo apkInfo, int resultCode) {
        getString(R.string.install_finished_with_result_code, resultCode);
        btnInstall.setEnabled(false);
        btnSilently.setEnabled(false);
        layoutTitleContainer.setBackgroundColor(settingsColors[0]);
        if (resultCode == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.apk_installed, apkInfo.getAppName()), Toast.LENGTH_SHORT).show();
            tvAppName.setText(R.string.successful);
            btnInstall.setEnabled(true);
            btnInstall.setText(R.string.open_app);
            layoutTitleContainer.setBackgroundColor(settingsColors[2]);
            if (!apkInfo.isFakePath() && Prefs.getPreference(this).getBoolean("auto_delete", false)) {
                Toast.makeText(this, getString(R.string.apk_deleteed, apkInfo.getApkFile().getName()), Toast.LENGTH_SHORT).show();
            }
        } else {
            tvAppName.setText(R.string.failed);
            layoutTitleContainer.setBackgroundColor(settingsColors[3]);
        }
        progressBar.setVisibility(View.INVISIBLE);
        layoutButtons.setVisibility(View.VISIBLE);
    }

    @Override
    public void onInstallLog(ApkInfo apkInfo, String logText) {
        layoutAppDetails.addView(createAppInfoView(logText, null));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_install:
                if (btnInstall.getText().toString().equalsIgnoreCase(getString(R.string.open_app))) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(apkCommander.getApkInfo().getPackageName());
                    startActivity(intent);
                    finish();
                } else {
                    apkCommander.startInstall();
                }
                break;
            case R.id.btn_silently:
                Intent intent = new Intent(this, BackgroundInstallActivity.class);
                intent.setData(getIntent().getData());
                startActivity(intent);
                finish();
                break;
            case R.id.btn_cancel:
                finish();
                break;
        }
    }
}
