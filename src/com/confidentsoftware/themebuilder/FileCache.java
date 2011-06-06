package com.confidentsoftware.themebuilder;

import java.io.File;

import android.content.Context;

public class FileCache {

	private static final FileCache sInstance = new FileCache();
	
	public static final int TYPE_THUMB = 1;
	public static final int TYPE_IMAGE = 2;

	private File thumbCacheDir;
	private File imageCacheDir;

	public static FileCache getInstance() {
		return sInstance;
	}

	public void onCreate(Context context) {
		thumbCacheDir = new File(context.getCacheDir(), "thumbs");
		thumbCacheDir.mkdir();
		imageCacheDir = new File(context.getCacheDir(), "images");
		imageCacheDir.mkdir();
	}
	
	public File get(int cacheType, String id) {
		if (cacheType == TYPE_THUMB) {
			return new File(thumbCacheDir, id);
		} else {
			return new File(imageCacheDir, id);
		}
	}
}
