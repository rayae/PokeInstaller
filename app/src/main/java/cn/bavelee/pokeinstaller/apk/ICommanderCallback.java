package cn.bavelee.pokeinstaller.apk;

import android.net.Uri;

import cn.bavelee.pokeinstaller.apk.ApkInfo;

public interface ICommanderCallback {
    void onStartParseApk(Uri uri);

    void onApkParsed(ApkInfo apkInfo);

    void onApkPreInstall(ApkInfo apkInfo);

    void onApkInstalled(ApkInfo apkInfo, int resultCode);

    void onInstallLog(ApkInfo apkInfo, String logText);
}
