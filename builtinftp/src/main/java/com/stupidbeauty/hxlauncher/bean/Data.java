package com.stupidbeauty.hxlauncher.bean;

import java.util.ArrayList;

@SuppressWarnings({"EmptyMethod", "unused", "CanBeFinal", "MismatchedQueryAndUpdateOfCollection"})
public class Data
{
	public String getLoremIpsum() {
		return loremIpsum;
	}

	private String loremIpsum;
	public int getImageId() {
		return imageId;
	}

	private int imageId;
	public boolean isLs() {
		return ls;
	}

	boolean ls=false; //!<是否是最终结果。

}
