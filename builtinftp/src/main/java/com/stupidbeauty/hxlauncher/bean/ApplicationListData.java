package com.stupidbeauty.hxlauncher.bean;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import com.stupidbeauty.hxlauncher.asynctask.InstalledPackageLoadTask;
import com.stupidbeauty.hxlauncher.InstalledPackageLoadTaskInterface;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import com.stupidbeauty.hxlauncher.interfaces.LocalServerListLoadListener;

public class ApplicationListData implements Serializable,InstalledPackageLoadTaskInterface
{
	private HashSet<String> urlSet=new HashSet<>(); //!<网址集合。

	public HashMap<String, Drawable> getLaunchIconMap() {
		return launchIconMap;
	}

	private final HashMap<String,Drawable> launchIconMap=new HashMap<>(); //!<启动图标缓存。

	/**
	 * 删除被卸载的软件包。
	 */
	public void removePackage(String packageName)
	{
		for (PackageInfo packageInfo: packageInfoList) //一个个地检查。
		{
			String currentPackageName=packageInfo.packageName; //获取软件包名字。

			if (currentPackageName.equals(packageName)) //正是这个软件包被卸载了。
			{
				packageInfoList.remove(packageInfo); //从列表中删除。

				break; //跳出。
			} //if (currentPackageName.equals(packageName)) //正是这个软件包被卸载了。
		} //for (PackageInfo packageInfo:packageInfoList) //一个个地检查。

		notifyApplicationList();
	} //public void removePackage(int uid)

	/**
	 * 删除被卸载的软件包。
	 * @param uid 被卸载的软件包的用户编号。
     */
	public void removePackage(int uid)
	{
		PackageManager packageManager=getPackageManager(); //获取软件包管理器。

		String[] packageNames=packageManager.getPackagesForUid(uid); //获取对应的软件包列表。

		if (packageNames!=null) //软件包列表存在。
		{
			for (String packageName:packageNames) //一个个地遍历。
			{
				try
				{
					PackageInfo packageInfo=packageManager.getPackageInfo(packageName,0); //获取对应的软件包信息。

					Intent launchIntent= packageManager.getLaunchIntentForPackage(packageName); //获取当前软件包的启动意图。

					if (launchIntent!=null) //有启动意图。
					{
						packageInfoList.add(packageInfo); //加入到列表中。


					} //else //有启动意图。
				}
				catch (PackageManager.NameNotFoundException e) //未找到该软件包。
				{
					e.printStackTrace(); //报告错误。
				} //catch (PackageManager.NameNotFoundException e) //未找到该软件包。
			} //for (String packageInfo:packageNames) //一个个地遍历。
		} //if (packageNames!=null) //软件包列表存在。


		notifyApplicationList();
	} //public void removePackage(int uid)
	
	/**
	* 加入外部存储上的软件包列表。
	*/
	public void addExternalPackageList(CharSequence[] newPackageNameList)
	{
	String[] packageNames=(String[])(newPackageNameList); //获取软件包名字数组。
	
        addPackageList(packageNames); //添加软件包列表。
	
	} //public void addExternalPackageList(List<String> newPackageNameList)
	
	/**
	* 添加软件包列表。
	*/
	private void addPackageList(String[] packageNames)
	{
		PackageManager packageManager=getPackageManager(); //获取软件包管理器。

		for (String packageName:packageNames) //一个个地遍历。
		{
			try
			{
				PackageInfo packageInfo=packageManager.getPackageInfo(packageName,0); //获取对应的软件包信息。

				Intent launchIntent= packageManager.getLaunchIntentForPackage(packageName); //获取当前软件包的启动意图。

				if (launchIntent!=null) //有启动意图。
				{
					packageInfoList.add(packageInfo); //加入到列表中。

					ApplicationInfo applicationInfo=packageInfo.applicationInfo; //获取应用程序信息。

					Drawable result; //结果。


					result=packageManager.getApplicationIcon(applicationInfo); //获取图标。

					launchIconMap.put(packageName,result); //加入缓存。

				} //else //有启动意图。
			}
			catch (PackageManager.NameNotFoundException e) //未找到该软件包。
			{
				e.printStackTrace(); //报告错误。
			} //catch (PackageManager.NameNotFoundException e) //未找到该软件包。
		} //for (String packageInfo:packageNames) //一个个地遍历。

		notifyApplicationList(); //通知应用程序列表获取完毕。
	} //public void addPackageList(String[] packageNames)

	/**
	 * 加入新安装的软件包。
	 * @param uid 软件包的用户编号。
     */
	public void addNewlyAddedPackage(int uid)
	{
		PackageManager packageManager=getPackageManager(); //获取软件包管理器。

		String[] packageNames=packageManager.getPackagesForUid(uid); //获取对应的软件包列表。

		if (packageNames!=null) //软件包列表存在。
		{
            addPackageList(packageNames); //添加软件包列表。
		} //if (packageNames!=null) //软件包列表存在。
	} //public void addNewlyAddedPackage(int uid)

	@Override
	public PackageManager getPackageManager() {
		return mContext.getPackageManager();
	}

	@Override
	public void processApplicationInfoLoadResult(List<PackageInfo> result) {
		packageInfoList.clear();
		packageInfoList.addAll(result);

		notifyApplicationList();
	}

	/**
	 * 将对象加入到本地服务器载入完毕的回调列表中。
	 * @param localServerListLoadListener 要加入的回调对象。
	 */
	public void addLocalServerListLoadListener(LocalServerListLoadListener localServerListLoadListener)
	{
		localServerListLoadListenerList.add(localServerListLoadListener); //加入列表。
	} //public void addLocalServerListLoadListener(LocalServerListLoadListener localServerListLoadListener)

	private final ArrayList<LocalServerListLoadListener> localServerListLoadListenerList=new ArrayList<>(); //!<本地服务器列表载入完毕监听器列表。

	/**
	 * 设置显示数据。
	 */
	private void notifyApplicationList()
	{
		for(LocalServerListLoadListener localServerListLoadListener:localServerListLoadListenerList) //一个个地通知。
		{
			localServerListLoadListener.onLoadPackageInfoList(); //载入完毕。
		} //for(LocalServerListLoadListener localServerListLoadListener:localServerListLoadListenerList) //一个个地通知。
	} //private void notifyApplicationList()

	private final Context mContext; //!<上下文。

	public ApplicationListData(Context context) {
		mContext=context;
	}

	/**
	 * 载入应用程序列表。
	 */
	public void loadApplicationList()
	{
		InstalledPackageLoadTask translateRequestSendTask =new InstalledPackageLoadTask(this); //创建异步任务。

		translateRequestSendTask.execute(); //执行任务。
	} //private void loadApplicationList()

	private final List<PackageInfo> packageInfoList = new ArrayList<>(); //!<软件包列表。

	public List<PackageInfo> getPackageInfoList() 
	{
		return packageInfoList;
	}

	/**
	 * 是否已经包含这个网址。
	 * @param fullUrl 要检查的网址。
	 * @return 是否已经包含。
	 */
	public boolean containsUrl(String fullUrl)
	{
		return urlSet.contains(fullUrl);
	} //public boolean containsUrl(String fullUrl)

	/**
	 * 记录，已经请求下载这个网址。
	 * @param fullUrl 完整的网址。
	 */
	public void addUrl(String fullUrl)
	{
		urlSet.add(fullUrl);
	} //public void addUrl(String fullUrl)


	/**
	 * 删除网址
	 * @param fullUrl 要删除的网址
	 */
	public void removeUrl(String fullUrl)
	{
		urlSet.remove(fullUrl); //删除
	} //public void removeUrl(String fullUrl)

}
