package com.stupidbeauty.hxlauncher.asynctask;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import com.stupidbeauty.hxlauncher.InstalledPackageLoadTaskInterface;
import java.util.List;

/**
 * @author Hxcan
 * @since Mar 13, 2014
 */
public final class InstalledPackageLoadTask extends AsyncTask<Object, Void, List<PackageInfo>>
{
  private final InstalledPackageLoadTaskInterface loadTaskIssuer; //!<主活动对象。

  public InstalledPackageLoadTask(InstalledPackageLoadTaskInterface activity)
  {
    loadTaskIssuer =activity; //记录。
  } //public TranslateRequestSendTask(OptimizeRepairSimpleActivity activity)

  @Override
  protected List<PackageInfo> doInBackground(Object... params)
  {
    List<PackageInfo> result; //结果。

    PackageManager packageManager= loadTaskIssuer.getPackageManager(); //获取软件包管理器。

    result=packageManager.getInstalledPackages(0);

    return result;
  } //catch (OutOfMemoryError error) //内存不足。

  /**
  * 报告结果。
  * @param result 结果。是否成功。
  */
		@Override
		protected void onPostExecute(List<PackageInfo> result)
        {
            loadTaskIssuer.processApplicationInfoLoadResult(result); //报告结果，翻译请求的发送结果。
		} //protected void onPostExecute(Boolean result)
	}



