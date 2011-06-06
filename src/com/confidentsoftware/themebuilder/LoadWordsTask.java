package com.confidentsoftware.themebuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.confidentsoftware.themebuilder.content.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class LoadWordsTask extends AsyncTask<Void, Void, Void> {

	private static final String TAG = "LoadWordsTask";

	private Context mContext;
	private BufferedReader mReader;
	private Uri mThemeUri;

	private DatabaseHelper mDbHelper;
	private FlickrParser mSiteParser;

	private ContentResolver mCr;
	private Exception mThrownException;

	public LoadWordsTask(Context context, BufferedReader reader, Uri themeUri)
			throws SQLException {
		mContext = context;
		mReader = reader;
		mThemeUri = themeUri;

		mCr = mContext.getContentResolver();

		mDbHelper = (DatabaseHelper) OpenHelperManager.getHelper(context,
				DatabaseHelper.class);
		mSiteParser = new FlickrParser(mContext, mDbHelper);
	}

	@Override
	protected Void doInBackground(Void... params) {

		try {
			String line = mReader.readLine();
			int i = 0;
			while (line != null) {
				loadImages(line);
				if (isCancelled()) {
					break;
				}
				line = mReader.readLine();
			}
		} catch (Exception e) {
			mThrownException = e;
		}
		return null;
	}

	private void loadImages(String text) throws SQLException {

		Uri wordsUri = Uri.withAppendedPath(mThemeUri, Dictionary.PATH_WORDS);
		ContentValues values = new ContentValues();
		values.put(Dictionary.Words.TEXT, text);
		// TODO don't hardcode language
		values.put("language_id", "en");
		Uri wordUri = mCr.insert(wordsUri, values);

		mSiteParser.getImagesForWord(ContentUris.parseId(wordUri), text);
	}

	@Override
	protected void onPostExecute(Void images) {
		try {
			mReader.close();
		} catch (IOException e) {
			// ignore
		}
		OpenHelperManager.releaseHelper();
		if (mThrownException != null) {
			Log.e(TAG, "Error parsing words", mThrownException);
		}
	}
}
