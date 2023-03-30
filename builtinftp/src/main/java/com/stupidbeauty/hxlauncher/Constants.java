package com.stupidbeauty.hxlauncher;

import android.os.Environment;
import java.io.File;

/**
 * 一些常量的定义。
 * @author root 蔡火胜。
 *
 */
public class Constants 
{
  /**
  * 文件路径。
  * @author root 蔡火胜。
  */
  public static class FilePath
  {
    public static final String UnknownDeviceMacAddr = "UnknownMacAddr"; //!<未知的网卡物理地址。
  } //public static class FilePath

  public static class Literal
  {
    public static final String PASSWORD_CX = "236000"; //!< 默认解锁密码。
  }

  /**
  * Operations。
  * @author root 蔡火胜。
  *
  */
  public final class Operation
  {
    public static final String TestShutDown = "com.stupidbeauty.shutdownat2100.testShutDown"; //!< 测试关机。
    public static final String ReportMessage="com.stupidbeauty.shutdownat2100.reportMessage"; //!<报告消息到来。
    public static final String PinShortcut="com.stupidbeauty.hxlauncher.pinShortcut"; //!<钉住快捷方式。
    public static final String ToggleBuiltinShortcuts="com.stupidbeauty.hxlauncher.toggleBuiltinShortcutss"; //!<切换是否显示内置快捷方式。
    public static final String ToggleHiveLayout="com.stupidbeauty.hxlauncher.toggleHiveLayout"; //!<切换是否要使用蜂窝布局
    public static final String UnlinkVoiceCommand="com.stupidbeauty.hxlauncher.unlinkVoiceCommand"; //!<断开语音指令的链接
  } // public final class Operation


    public final class Url
    {
      public static final String WebSocketServerUrl = "ws://192.168.0.108:20402/";

    } // public final class Url

    /**
     * native messages.
     * @author root
     */
    public final class NativeMessage
    {
      public static final String APPLICATION_LAUNCHED = "com.stupidbeauty.hxlauncher.constants.nativemessage.applicationLaunched"; //!< 应用程序被启动。
      public static final String APPLICATION_LAUNCHED_PACKAGE_KEY = "com.stupidbeauty.hxlauncher.constants.nativemessage.applicationLaunchedPackageKey"; //!<被启动的应用程序的包名的参数键。
    } //public final class NativeMessage

    /**
     * 目录路径。
     * @author root 蔡火胜。
     *
     */
    public static class DirPath
    {
        public static final String FARMING_BOOK_APP_SD_CARD_PATH = Environment.getExternalStorageDirectory().getPath()+ File.separator+ "etc" + File.separator+"ShutDownAt2100"; //女神相机的路径。
        public static final String DCIM_SD_CARD_PATH = Environment.getExternalStorageDirectory().getPath()+ File.separator+ Environment.DIRECTORY_DCIM; //!<相册的路径。
    } //public static class DirPath

    /**
     * Action from lanime.
     * @author root
     *
     */
    public final class LanImeAction
    {
      public static final String InputtingForPackage = "com.stupidbeauty.lanime.inputtingforpackage"; //!< 正在为该个软件包做输入。
        public static final String PackageNameOfInputting = "com.stupidbeauty.lanime.packagenameofinputting"; //!<正在为其输入的软件包名字。

    } // public final class LanImeAction

    public final class Actions
    {
        public static final String LegacyInstallShortcut = "com.android.launcher.action.INSTALL_SHORTCUT"; //!<传统的安装快捷方式。
    }

    public final class Numbers
    {
      public static final int IgnoreVoiceResultLength=1; //!< 短于 这个长度的语音识别结果，不处理
    }

	/**
	 * Network information
	 * @author root 蔡火胜。
	 */
	public final class Networks
	{
		public static final String RabbitMQUserName="optimizerepair"; //!<RabbitMQ用户名。
		public static final String RabbitMQPassword="som3150"; //!<RabbitMQ密码。
		public static final String TRANSLATE_REQUEST_QUEUE_NAME = "com.stupidbeauty.hxlauncher.VoiceCommandHitDataQueue.cbor"; //!<语音命中应用事件报告队列名字。
		public static final String VoiceAssociationDataQueueName = "com.stupidbeauty.hxlauncher.VoiceAssociationDataQueue"; //!<语音关联数据报告队列名字。
		public static final String VoiceShortcutAssociationDataQueueName = "com.stupidbeauty.hxlauncher.VoiceShortcutAssociationDataQueue"; //!<语音关联快捷方式数据报告队列名字。
	} //public final class Networks
} // public class Constants
