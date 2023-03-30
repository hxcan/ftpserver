package com.stupidbeauty.hxlauncher.asynctask;

import java.util.ArrayList;
import java.util.HashMap;
// import com.stupidbeauty.upgrademanager.asynctask.LoadVoicePackageUrlMapInterface;
import java.util.List;
import android.content.pm.PackageItemInfo;
import java.util.List;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.Random;
import com.upokecenter.cbor.CBORObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import android.annotation.SuppressLint;
import com.stupidbeauty.hxlauncher.bean.VoicePackageUrlMapData;
import java.util.HashMap;
import android.view.View;
import android.os.AsyncTask;
import com.stupidbeauty.hxlauncher.bean.VoiceCommandHitDataObject;
import java.util.HashMap;
import android.net.Uri;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.os.AsyncTask;
import android.util.Log;
// import org.apache.commons.collections4.SetValuedMap;
// import com.google.protobuf.ByteString;
// import com.rabbitmq.client.Channel;
// import com.rabbitmq.client.Connection;
// import com.rabbitmq.client.ConnectionFactory;
// import com.rabbitmq.client.MessageProperties;
// import com.stupidbeauty.farmingbookapp.PreferenceManagerUtil;
import com.stupidbeauty.hxlauncher.bean.VoicePackageMapJsonItem;
// import com.stupidbeauty.hxlauncher.BuildConfig;
// import com.stupidbeauty.hxlauncher.VoiceCommandHitDataMessageProtos;
// import com.stupidbeauty.hxlauncher.VoicePackageMapItemMessageProtos;
// import com.stupidbeauty.hxlauncher.VoicePackageMapMessageProtos;
// import com.stupidbeauty.hxlauncher.application.HxLauncherApplication;
import com.stupidbeauty.hxlauncher.datastore.LauncherIconType;
import java.util.Set;
import java.util.HashSet;
// import org.apache.commons.collections4.MultiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Random;

/**
 * @author Hxcan
 * @since Mar 13, 2014
 */
public final class VoicePackageNameMapSaveTask extends AsyncTask<Object, Void, Boolean>
{
  private Context context= null; //!< 上下文
	private static final String TAG="VoicePackageNameMapSaveTask"; //!<输出调试信息时使用的标记。

  @Override
  protected Boolean doInBackground(Object... params)
  {
    //参数顺序：
    //            private MultiMap<String, PackageItemInfo> voicePackageNameMap; //!<语音识别结果与包条目信息之间的映射关系。本设备独有的
    //            voiceRecognizeResultString, packageName, activityName, recordSoundFilePath, iconType, iconTitle

    Boolean result=false; //结果，是否成功。

    //            String subject=(String)(params[0]); //获取识别结果文字内容。
    HashMap<String, Uri> voicePackageNameMap=(HashMap<String, Uri>)(params[0]); // 获取映射对象
    context= (Context)(params[1]); // 获取上下文



    boolean addPhotoFile=false; //Whether to add photo file

    Log.d(TAG,"1129, saveVoicePackageNameMap, answer value: "); //Debug.

    byte[] serializedContent = constructVoiceCommandHistDataMessageCbor(voicePackageNameMap); // Construct the message byte array.

    //             byte[] serializedContent=translateRequestMessage.build().toByteArray(); //序列化成字节数组。

    Log.d(TAG,"1134, saveVoicePackageNameMap, answer value: content length: " + serializedContent.length); //Debug.


    File photoFile=findVoicePackageMapFile(); //寻找语音识别与软件包映射文件。

    Log.d(TAG,"143, saveVoicePackageNameMap, file path: " + photoFile.getAbsolutePath()); //Debug.

    try
    {
      FileUtils.writeByteArrayToFile(photoFile, serializedContent); //写入内容。

        Log.d(TAG,"149, saveVoicePackageNameMap, file saved, length: " + photoFile.length()); //Debug.

      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

      Log.d(TAG,"1144, saveVoicePackageNameMap, answer value: "); //Debug.


      return result;
  }

    private byte[] constructVoiceCommandHistDataMessageCbor(HashMap<String, Uri> subject)
    {
      VoicePackageUrlMapData translateRequestBuilder = new VoicePackageUrlMapData(); // 创建消息构造器。
      
      List<VoicePackageMapJsonItem> virtualPathMapList=new ArrayList<>();
      
      List<String> virtualPathList= new ArrayList<>(subject.keySet()); // Get the key list.
      
      for(String currentVirtualPath: virtualPathList) // Add the virtual paths one by one.
      {
        VoicePackageMapJsonItem currentVirtualPathMapItem=new VoicePackageMapJsonItem(); // Create map item.
        
        Uri curentUri=subject.get(currentVirtualPath); // The uri.
        Log.d(TAG, CodePosition.newInstance().toString()+  ", path: " + currentVirtualPath + ", uri: " + curentUri); // Debug.

        currentVirtualPathMapItem.setVirtualPath(currentVirtualPath); // SEt virtual path.
        currentVirtualPathMapItem.setUri(curentUri); // SEt Uri.
      
        virtualPathMapList.add(currentVirtualPathMapItem); // Add the item.
      } // for(String currentVirtualPath: virtualPathList) // Add the virtual paths one by one.

      translateRequestBuilder.setVoicePackageMapJsonItemList(virtualPathMapList); // 设置 the list.

        


      try //尝试构造请求对象，并且捕获可能的异常。
      {

        byte[] photoBytes= null; //将照片文件内容全部读取。


            



      } //try //尝试构造请求对象，并且捕获可能的异常。
      catch (Exception e)
      {
        e.printStackTrace();
      }

      boolean addPhotoFile=false; //Whether to add photo file

      CBORObject cborObject= CBORObject.FromObject(translateRequestBuilder); //创建对象

      byte[] array=cborObject.EncodeToBytes();

      String arrayString=new String(array);

      Log.d(TAG, "constructVoiceCommandHistDataMessageCbor, message array lngth: " + array.length); //Debug.

      return array;
    } //private byte[] constructVoiceCommandHistDataMessageCbor(String subject, String body, String acitivtyName, LauncherIconType iconType, String iconTitle, File photoFile)

    /**
     * 寻找语音识别与软件包映射文件。
     * @return 语音识别与软件包映射文件。
     */
    private  File findVoicePackageMapFile()
    {
        File result=null;

//         Context context= HxLauncherApplication.getAppContext(); //获取上下文

        File filesDir=context.getFilesDir();

        Log.d(TAG, "1459, findRandomPhotoFile, files dir: "+ filesDir); //Debug.

        if (filesDir==null) //该目录不存在。
        {

        } //if (filesDir==null) //该目录不存在。
        else //该目录存在。
        {
            result=new File(filesDir.getAbsolutePath()+"/voicePackageNameMap.proto"); //指定文件名。

            Log.d(TAG, "1469, findRandomPhotoFile, files exists: "+ result.exists() + ", size: " + result.length()); //Debug.

            if (result.exists()) //文件存在。
            {

            } //if (result.exists()) //文件存在。
            else //文件不存在。
            {
                try
                {
                    boolean createResult=result.createNewFile(); //创建文件。

                    Log.d(TAG, "findRandomPhotoFile, create file result: " + createResult); //Debug.

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
     * 报告结果。
     * @param result 结果。是否成功。
     */
		@Override
		protected void onPostExecute(Boolean result)
        {


		} //protected void onPostExecute(Boolean result)
	}



