package com.stupidbeauty.hxlauncher;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.List;

//import android.support.v7.app.AppCompatActivity;


@SuppressWarnings({"WeakerAccess", "EmptyMethod", "unused", "UnusedAssignment"})
public interface InstalledPackageLoadTaskInterface
{

     void processApplicationInfoLoadResult(List<PackageInfo> result);

    PackageManager getPackageManager();

}
