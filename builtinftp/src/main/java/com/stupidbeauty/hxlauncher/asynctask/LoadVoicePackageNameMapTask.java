package com.stupidbeauty.hxlauncher.asynctask;

import android.content.Context;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import android.net.Uri;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import android.annotation.SuppressLint;
import com.stupidbeauty.hxlauncher.bean.VoicePackageUrlMapData;
import android.net.Uri;
// import com.stupidbeauty.hxlauncher.manager.ActiveUserReportManager;
import android.os.Debug;
// import com.stupidbeauty.hxlauncher.asynctask.LoadBuiltinVoicePackageNameMapTask;
// import com.stupidbeauty.hxlauncher.asynctask.BuildActivityLabelPackageItemInfoMapTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import static android.content.Intent.ACTION_PACKAGE_CHANGED;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import android.view.View;
import android.os.AsyncTask;
// import com.stupidbeauty.hxlauncher.LauncherActivity;
import java.util.HashMap;
import com.stupidbeauty.hxlauncher.bean.ApplicationNamePair;
import java.util.List;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.AnimationDrawable;
// import org.apache.commons.collections4.SetValuedMap;
import android.util.Pair;
// import androidx.localbroadcastmanager.content.LocalBroadcastManager;
// import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
// import com.andexert.library.RippleView;
// import com.stupidbeauty.hxlauncher.AndroidApplicationMessage;
// import com.stupidbeauty.hxlauncher.VoicePackageMapItemMessageProtos;
// import com.stupidbeauty.hxlauncher.VoicePackageMapMessageProtos;

public class LoadVoicePackageNameMapTask extends AsyncTask<Object, Void, Object>
{
  private HashMap<String, Uri> voicePackageNameMap; //!< The map of virtual path to Uri.
  private Context context= null; //!< 上下文

	private static final String TAG="LoadVoicePackageNameMapTask"; //!<输出调试信息时使用的标记。

	private VirtualPathLoadInterface launcherActivity=null; //!< The interface of virtual path load result.
	
    /**
     * 寻找语音识别与软件包映射文件。
     * @return 语音识别与软件包映射文件。
     */
    private  File findVoicePackageMapFile()
    {
      File result=null;

      File filesDir= context.getFilesDir();

      if (filesDir==null) //该目录不存在。
      {
      } //if (filesDir==null) //该目录不存在。
      else //该目录存在。
      {
        result=new File(filesDir.getAbsolutePath()+"/voicePackageNameMap.proto"); //指定文件名。

        if (result.exists()) //文件存在。
        {
        } //if (result.exists()) //文件存在。
        else //文件不存在。
        {
          try
          {
            result.createNewFile(); //创建文件。
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        } //else //文件不存在。
      } //else //该目录存在。

        return result;
    } //private  File findRandomPhotoFile()

	    /**
     * 载入语音识别结果与包名之间的映射。
     */
    private void loadVoicePackageNameMap()
    {
      File photoFile= findVoicePackageMapFile(); //随机寻找一个照片文件。

      voicePackageNameMap=new HashMap<>(); // 创建映射。

      if (photoFile!=null) //不是空指针。
      {
        if (photoFile.exists()) //文件存在。
        {
          try
          {
            byte[] photoBytes= FileUtils.readFileToByteArray(photoFile); //将照片文件内容全部读取。

            
            
            CBORObject videoStreamMessage= CBORObject.DecodeFromBytes(photoBytes); //解析消息。
                
            Collection<CBORObject> subFilesList=videoStreamMessage.get("voicePackageMapJsonItemList").getValues();
        






            for (CBORObject currentSubFile: subFilesList) //一个个子文件地比较其文件名。
            {
              String currentRelationshipgetVoiceRecognizeResult=currentSubFile.get("virtualPath").AsString(); // Get virutal path.
              String uriString=currentSubFile.get("uri").AsString(); // Get the uri.
              String packageName=currentSubFile.get("packageName").AsString();
              String informationUrl=currentSubFile.get("informationUrl").AsString(); // 获取信息页面地址。
              Uri currentPackageItemInfo=Uri.parse(uriString); // Parse the uri.
                    
              CBORObject versionNameObject=currentSubFile.get("versionName");
            
              if (versionNameObject!=null)
              {
                String versionName=versionNameObject.AsString();

              } //versionNameObject
                




      
      
              Log.d(TAG, CodePosition.newInstance().toString()+  ", path: " + currentRelationshipgetVoiceRecognizeResult + ", uri: " + currentPackageItemInfo); // Debug.
              voicePackageNameMap.put(currentRelationshipgetVoiceRecognizeResult, currentPackageItemInfo); //加入映射。

            } //for (FileMessageContainer.FileMessage currentSubFile:videoStreamMessage.getSubFilesList()) //一个个子文件地比较其

            //     Log.d(TAG, "loadVoicePackageUrlMapCbor, packageNameApplicationNameMap list size: "+ packageNameApplicationNameMap.size()); //Debug.
            Log.d(TAG, CodePosition.newInstance().toString()+  ", voicePackageNameMap size: " + voicePackageNameMap.size()); // Debug.

            
            
            // Old:
            
            //             VoicePackageMapMessageProtos.VoicePackageMapMessage translateRequestMessage=VoicePackageMapMessageProtos.VoicePackageMapMessage.parseFrom(photoBytes); //创建一个消息对象。
            // 
            //             List<VoicePackageMapItemMessageProtos.VoicePackageMapItemMessage> relationships=translateRequestMessage.getMapList(); //获取关系列表。
            // 
            //             for(VoicePackageMapItemMessageProtos.VoicePackageMapItemMessage currentRelationship: relationships) //一个个地加入映射中。
            //             {
            //               PackageItemInfo currentPackageItemInfo=new PackageItemInfo(); //当前的包条目信息对象。
            // 
            //               currentPackageItemInfo.packageName=currentRelationship.getPackageName();
            //               currentPackageItemInfo.name=currentRelationship.getActivityName(); //记录活动名字。
            // 
            //               voicePackageNameMap.put(currentRelationship.getVoiceRecognizeResult(), currentPackageItemInfo); //加入映射。
            // 
            //               List<AndroidApplicationMessage> applicationMessages=currentRelationship.getApplicationInformationList(); //获取多映射目标列表
            // 
            //               for(AndroidApplicationMessage currentApplicationMessage: applicationMessages) //一个个地加入映射目标
            //               {
            //                 PackageItemInfo currentPackageItemInfoA=new PackageItemInfo(); //当前的包条目信息对象。
            // 
            //                 currentPackageItemInfoA.packageName=currentApplicationMessage.getPackageName();
            //                 currentPackageItemInfoA.name=currentApplicationMessage.getActivityName(); //记录活动名字。
            // 
            //                 voicePackageNameMap.put(currentRelationship.getVoiceRecognizeResult(), currentPackageItemInfoA); //加入映射。
            //               } //for(AndroidApplicationMessage currentApplicationMessage: applicationMessages) //一个个地加入映射目标
            //             } //for(TranslateRequestMessageProtos.TranslateRequestMessage currentRelationship: relationships) //一个个地加入映射中。
          }
          catch (IOException e)
          {
            e.printStackTrace();
          } //catch (IOException e)
          catch (CBORException e)
          {
            e.printStackTrace();
          } //catch (IOException e)
        } //if (photoFile.exists()) //文件存在。
      } //if (photoFile!=null) //不是空指针。
    } //private void loadVoicePackageNameMap()

    @Override
    protected Object doInBackground(Object... params)
    {
      //参数顺序：
      //            private MultiMap<String, PackageItemInfo> voicePackageNameMap; //!<语音识别结果与包条目信息之间的映射关系。本设备独有的
      //            voiceRecognizeResultString, packageName, activityName, recordSoundFilePath, iconType, iconTitle

      Boolean result=false; //结果，是否成功。

      //            String subject=(String)(params[0]); //获取识别结果文字内容。
      //             SetValuedMap<String, PackageItemInfo> voicePackageNameMap=(SetValuedMap<String, PackageItemInfo>)(params[0]); //获取映射对象
      launcherActivity=(VirtualPathLoadInterface)(params[0]); // 获取映射对象
      context= (Context)(params[1]); // 获取上下文

      loadVoicePackageNameMap(); // 载入映射。
        
      //             buildInternationalizationDataPackageNameMap(); // 构造映射。

      boolean addPhotoFile=false; //Whether to add photo file

      return voicePackageNameMap;
    }

    /**
     * 报告结果。
     * @param result 结果。是否成功。
     */
		@Override
		protected void onPostExecute(Object result)
    {
      launcherActivity.setVoicePackageNameMap(voicePackageNameMap);
		} //protected void onPostExecute(Boolean result)
	}
