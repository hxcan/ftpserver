package com.stupidbeauty.hxlauncher.callback;

import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.UserHandle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.util.Log;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LauncherAppsCallback extends LauncherApps.Callback
{
    private static final String TAG="LauncherAppsCallback"; //!<输出调试信息时使用的标记。

    @Override
    public void onShortcutsChanged(@NonNull String packageName, @NonNull List<ShortcutInfo> shortcuts, @NonNull UserHandle user) {
        super.onShortcutsChanged(packageName, shortcuts, user);
        Log.d(TAG, "onShortcutsChanged, package name: " + packageName+ ", shortcuts: " + shortcuts + ", user: " + user); //Debug.
    }

    @Override
    public void onPackageRemoved(String s, UserHandle userHandle) {
    }

    @Override
    public void onPackageAdded(String s, UserHandle userHandle) {
    }

    @Override
    public void onPackageChanged(String s, UserHandle userHandle) {

    }

    @Override
    public void onPackagesAvailable(String[] strings, UserHandle userHandle, boolean b) {
    }

    @Override
    public void onPackagesUnavailable(String[] strings, UserHandle userHandle, boolean b) {

    }
}
