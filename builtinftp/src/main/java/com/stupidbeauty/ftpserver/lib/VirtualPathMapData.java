package com.stupidbeauty.ftpserver.lib;

import java.util.ArrayList;
import java.util.List;

public class VirtualPathMapData
{
	private List<VirtualPathMapItem> voicePackageMapJsonItemList=new ArrayList<>(); //!<语音识别结果与软件包下载地址映射JSON条目列表。

	public List<VirtualPathMapItem> getVoicePackageMapJsonItemList() 
	{
		return voicePackageMapJsonItemList;
	}
	
	public void setVoicePackageMapJsonItemList( List<VirtualPathMapItem>  listToSet) 
	{
		voicePackageMapJsonItemList=listToSet;
	}
}




























