package com.stupidbeauty.hxlauncher.asynctask;

import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Handler;
// import com.stupidbeauty.shutdownat2100.helper.ShutDownAt2100Manager;
// import com.android.volley.RequestQueue;
// import com.android.volley.Response;
// import com.android.volley.VolleyError;
// import com.google.protobuf.InvalidProtocolBufferException;
// import com.stupidbeauty.hxlauncher.activity.ApplicationUnlockActivity;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import android.net.Uri;
// import com.stupidbeauty.hxlauncher.manager.ActiveUserReportManager;
import android.os.Debug;
// import com.stupidbeauty.hxlauncher.asynctask.LoadBuiltinVoicePackageNameMapTask;
// import com.stupidbeauty.hxlauncher.asynctask.BuildActivityLabelPackageItemInfoMapTask;
// import com.stupidbeauty.hxlauncher.asynctask.LoadBuiltinShortcutsTask;
// import com.stupidbeauty.hxlauncher.asynctask.LoadPreferenceTask;
// import com.stupidbeauty.hxlauncher.asynctask.LoadShortcutsTask;
// import com.stupidbeauty.hxlauncher.asynctask.ReqGameDataTask;
// import com.stupidbeauty.hxlauncher.asynctask.BindAdapterTask;
// import com.stupidbeauty.hxlauncher.asynctask.LoadBuiltinVoiceShortcutMapTask;
// import com.stupidbeauty.hxlauncher.asynctask.LoadServerVoiceCommandReponseIgnoreTask;
// import com.stupidbeauty.hxlauncher.asynctask.LoadVoiceShortcutIdMapTask;
import com.stupidbeauty.hxlauncher.asynctask.LoadVoicePackageNameMapTask;
import java.util.Timer;
import java.util.TimerTask;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.LocaleList;
import android.os.PowerManager;
// import android.os.Vibrator;
import android.provider.Settings;
// import org.apache.commons.collections4.SetValuedMap;
import android.util.Pair;
// import androidx.localbroadcastmanager.content.LocalBroadcastManager;
// import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
// import com.andexert.library.RippleView;
// import com.example.administrator.douyin.Love2;
// import androidx.recyclerview.widget.RecyclerView;
// import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
// import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
// import com.stupidbeauty.hxlauncher.asynctask.BuildInternationalizationDataPackageNameMapTask;
// import com.stupidbeauty.hxlauncher.activity.ApplicationInformationActivity;
// import com.stupidbeauty.hxlauncher.adapter.FlipAnimationAdapter;
import com.stupidbeauty.hxlauncher.asynctask.VoicePackageNameMapSaveTask;
import com.stupidbeauty.hxlauncher.bean.VoiceCommandHitDataObject;
// import com.android.volley.RequestQueue;
// import com.google.gson.Gson;
// import com.google.protobuf.ByteString;
// import com.huiti.msclearnfootball.AnswerAvailableEvent;
// import com.huiti.msclearnfootball.VoiceRecognizeResult;
// import com.iflytek.cloud.ErrorCode;
// import com.iflytek.cloud.RecognizerListener;
// import com.iflytek.cloud.RecognizerResult;
// import com.iflytek.cloud.SpeechConstant;
// import com.iflytek.cloud.SpeechError;
// import com.iflytek.cloud.SpeechUtility;
// import com.stupidbeauty.builtinftp.BuiltinFtpServer;
// import com.stupidbeauty.hxlauncher.service.DownloadNotificationService; 
// import com.stupidbeauty.farmingbookapp.PreferenceManagerUtil;
// import com.stupidbeauty.hxlauncher.asynctask.TranslateRequestSendTask;
// import com.stupidbeauty.comgooglewidevinesoftwaredrmremover.app.LanImeUncaughtExceptionHandler;
// import com.stupidbeauty.grebe.DownloadRequestor;
// import com.stupidbeauty.hxlauncher.application.HxLauncherApplication;
// import com.stupidbeauty.hxlauncher.asynctask.VoiceAssociationDataSendTask;
// import com.stupidbeauty.hxlauncher.asynctask.VoiceShortcutAssociationDataSendTask;
import com.stupidbeauty.hxlauncher.bean.ApplicationNameInternationalizationData;
import com.stupidbeauty.hxlauncher.bean.ApplicationNamePair;
import com.stupidbeauty.hxlauncher.bean.HxShortcutInfo;
import com.stupidbeauty.hxlauncher.callback.LauncherAppsCallback;
import com.stupidbeauty.hxlauncher.datastore.LauncherIconType;
import com.stupidbeauty.hxlauncher.datastore.RuntimeInformationStore;
import com.stupidbeauty.hxlauncher.datastore.VoiceCommandSourceType;
// import com.stupidbeauty.hxlauncher.external.ShutDownAt2100Manager;
// import com.stupidbeauty.qtdocchinese.ArticleInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

// import butterknife.Bind;
// import butterknife.ButterKnife;
// import butterknife.OnClick;

import com.stupidbeauty.hxlauncher.interfaces.LocalServerListLoadListener;

import com.stupidbeauty.hxlauncher.bean.ApplicationListData;
// import com.iflytek.cloud.SpeechRecognizer;
// import com.stupidbeauty.victoriafresh.VFile;
// import com.stupidbeauty.hxlauncher.rpc.CloudRequestorZzaqwb;

// import org.apache.commons.collections4.MultiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import static android.content.Intent.ACTION_PACKAGE_CHANGED;
import static android.content.Intent.ACTION_PACKAGE_REPLACED;
import static android.content.Intent.EXTRA_COMPONENT_NAME;
import static android.content.Intent.EXTRA_PACKAGE_NAME;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED_BY_ANY_LAUNCHER;
import static com.stupidbeauty.hxlauncher.Constants.Actions.LegacyInstallShortcut;
import static com.stupidbeauty.hxlauncher.Constants.LanImeAction.InputtingForPackage;
import static com.stupidbeauty.hxlauncher.Constants.LanImeAction.PackageNameOfInputting;
import static com.stupidbeauty.hxlauncher.Constants.Numbers.IgnoreVoiceResultLength;
import static com.stupidbeauty.hxlauncher.Constants.Operation.ToggleBuiltinShortcuts;
import static com.stupidbeauty.hxlauncher.Constants.Operation.ToggleHiveLayout;
import static com.stupidbeauty.hxlauncher.Constants.Operation.UnlinkVoiceCommand;
import static com.stupidbeauty.hxlauncher.datastore.LauncherIconType.ShortcutIconType;
import static com.stupidbeauty.hxlauncher.datastore.VoiceCommandSourceType.LocalVoiceCommandMap;
import static com.stupidbeauty.hxlauncher.datastore.VoiceCommandSourceType.ServerVoiceCommandResponse;
import android.os.Process;

public interface VirtualPathLoadInterface
{
    PowerManager.WakeLock wakeLock=null; //!<游戏辅助唤醒锁。
    
    int ret = 0;

    /**
    * 设置语音识别结果与包条目信息之间的映射关系。本设备独有的
    */
    public void  setVoicePackageNameMap (HashMap<String, Uri> voicePackageNameMap) ; 
}