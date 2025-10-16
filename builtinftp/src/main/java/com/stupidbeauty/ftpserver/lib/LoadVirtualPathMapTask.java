package com.stupidbeauty.ftpserver.lib;

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
import android.net.Uri;
import android.os.Debug;
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
import java.util.HashMap;
import java.util.List;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.AnimationDrawable;
import android.util.Pair;

public class LoadVirtualPathMapTask extends AsyncTask<Object, Void, Object>
{
  private HashMap<String, Uri> voicePackageNameMap; //!< The map of virtual path to Uri.
  private Context context= null; //!< 上下文

	private static final String TAG="LoadVirtualPathMapTask"; //!< The tag used for debug code.

	private VirtualPathLoadInterface launcherActivity=null; //!< The interface of virtual path load result.
	
    /**
     * 寻找 Virtual path 映射文件。
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
      } // else //该目录存在。

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
            Log.d(TAG, CodePosition.newInstance().toString()+  ", byte array length: " + photoBytes.length); // Debug.
            
            CBORObject videoStreamMessage= CBORObject.DecodeFromBytes(photoBytes); // 解析消息。
            String jsonString = videoStreamMessage.ToJSONString(); // Get the json string.
            Log.d(TAG, CodePosition.newInstance().toString()+  ", curent map item: " + jsonString); // Debug.
                
            Collection<CBORObject> subFilesList=videoStreamMessage.get("voicePackageMapJsonItemList").getValues();

            for (CBORObject currentSubFile: subFilesList) //一个个子文件地比较其文件名。
            {
              jsonString = currentSubFile.ToJSONString(); // Get the json string.
              Log.d(TAG, CodePosition.newInstance().toString()+  ", curent map item: " + jsonString); // Debug.
              
              CBORObject virtualPathObject=currentSubFile.get("virtualPath");
              
              if (virtualPathObject!=null) // The object exists
              {
                String currentRelationshipgetVoiceRecognizeResult=virtualPathObject.AsString(); // Get virutal path.
                String uriString=currentSubFile.get("uri").AsString(); // Get the uri.

                Uri currentPackageItemInfo=Uri.parse(uriString); // Parse the uri.
                      
                Log.d(TAG, CodePosition.newInstance().toString()+  ", path: " + currentRelationshipgetVoiceRecognizeResult + ", uri: " + currentPackageItemInfo); // Debug.
                voicePackageNameMap.put(currentRelationshipgetVoiceRecognizeResult, currentPackageItemInfo); //加入映射。
              } // if (virtualPathObject!=null) // The object exists
            } //for (FileMessageContainer.FileMessage currentSubFile:videoStreamMessage.getSubFilesList()) //一个个子文件地比较其

            Log.d(TAG, CodePosition.newInstance().toString()+  ", voicePackageNameMap size: " + voicePackageNameMap.size()); // Debug.
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
      // launcherActivity, context

      Boolean result=false; //结果，是否成功。

      launcherActivity=(VirtualPathLoadInterface)(params[0]); // 获取映射对象
      context= (Context)(params[1]); // 获取上下文

      loadVoicePackageNameMap(); // 载入映射。
        
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
