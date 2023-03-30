package com.stupidbeauty.hxlauncher.bean;

import java.util.ArrayList;
import java.util.List;

public class VoicePackageUrlMapData
{
	private List<VoicePackageMapJsonItem> voicePackageMapJsonItemList=new ArrayList<>(); //!<语音识别结果与软件包下载地址映射JSON条目列表。

	public List<VoicePackageMapJsonItem> getVoicePackageMapJsonItemList() 
	{
		return voicePackageMapJsonItemList;
	}
	
	public void setVoicePackageMapJsonItemList( List<VoicePackageMapJsonItem>  listToSet) 
	{
		voicePackageMapJsonItemList=listToSet;
	}
}




























