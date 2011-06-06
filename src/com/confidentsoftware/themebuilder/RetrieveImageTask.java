package com.confidentsoftware.themebuilder;

import java.io.File;
import java.io.IOException;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class RetrieveImageTask extends AsyncTask<String, Void, Drawable> {

	private static final String TAG = "RetrieveImageTask";

	private int mImageType;
	private ImageView mView;

	RetrieveImageTask(int imageType, ImageView view) {
		mImageType = imageType;
		mView = view;
	}

	@Override
	protected Drawable doInBackground(String... params) {
		String id = params[0];
		String url = params[1];
		File file = FileCache.getInstance().get(mImageType, id);
		Log.d(TAG, "Using file " + file);
		if (!file.exists()) {
			Log.d(TAG, "Downloading from " + url);
			try {
				file.createNewFile();
				NetworkClient.downloadFile(url, file);
			} catch (IOException e) {
				Log.w(TAG, "Could not download image " + id, e);
			}
		}
		return Drawable.createFromPath(file.getAbsolutePath());
	}

	@Override
	protected void onPostExecute(Drawable d) {
		mView.setImageDrawable(d);
	}
}
