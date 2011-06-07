package com.confidentsoftware.themebuilder;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.confidentsoftware.themebuilder.Dictionary.Images;
import com.confidentsoftware.themebuilder.Dictionary.Words;

/**
 * Query all of the English words in the main database and insert a
 * corresponding word in the given language. Also map the image that points to
 * the English word to the new translated word.
 * 
 * @author shawn.lauzon
 * 
 */
public class TranslatorTask extends AsyncTask<Void, Void, Void> {

	private static final String TAG = "Translator";

	private static final String TRANSLATE_URL = "https://www.googleapis.com/language/translate/v2?key="
			+ ApiKeys.GOOGLE_TRANSLATE + "&q=%s&source=%s&target=%s";

	private static final String[] IMAGES_PROJECTION = { Images._ID,
			Images.NUM_LIKES };

	private ContentResolver mCr;
	private Uri mThemeUri;
	private String mLanguage;

	public TranslatorTask(ContentResolver cr, Uri themeUri, String language) {
		mCr = cr;
		mThemeUri = themeUri;
		mLanguage = language;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Uri wordsInAThemeUri = Uri.withAppendedPath(mThemeUri,
				Dictionary.PATH_WORDS);
		Cursor cursor = mCr.query(wordsInAThemeUri, new String[] { Words.TEXT,
				Words._ID }, Words.LANGUAGE + " = ?", new String[] { "en" },
				Words.TEXT);
		String word = null;
		try {
			ContentValues values = new ContentValues();
			values.put(Words.LANGUAGE, mLanguage);
			while (cursor.moveToNext()) {
				try {
					word = cursor.getString(0);
					values.put(Words.TEXT, translate(word));
					Uri wordUri = mCr.insert(wordsInAThemeUri, values);
					copyImageLinks(wordUri, ContentUris.withAppendedId(
							Words.CONTENT_URI, cursor.getLong(1)));
				} catch (IOException e) {
					Log.w(TAG, "Error translating " + word, e);
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	private void copyImageLinks(Uri newWordUri, Uri oldWordUri) {
		Cursor cursor = mCr.query(Uri.withAppendedPath(oldWordUri,
				Dictionary.PATH_IMAGES), IMAGES_PROJECTION, null, null, null);
		try {
			ContentValues values = new ContentValues();
			while (cursor.moveToNext()) {
				values.put(Images.NUM_LIKES, cursor.getInt(1));
				mCr.insert(Words.CONTENT_URI.buildUpon().appendPath(
						newWordUri.getLastPathSegment()).appendPath(
						Dictionary.PATH_IMAGES).appendPath(cursor.getString(0))
						.build(), values);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private String translate(String word) throws IOException {
		String encodedWord = URLEncoder.encode(word, "utf-8");
		String url = String.format(TRANSLATE_URL, encodedWord, "en", mLanguage);
		String raw = NetworkClient.downloadString(url);
		String translatedWord = null;
		try {
			translatedWord = parseTranslatedJson(new JSONObject(raw));
		} catch (JSONException e) {
			Log.w(TAG, "Error parsing translation of " + word, e);
		}
		Log.v(TAG, "Translated " + encodedWord + " to " + translatedWord);
		return translatedWord;
	}

	private String parseTranslatedJson(JSONObject json) throws JSONException {
		JSONArray translations = json.getJSONObject("data").getJSONArray(
				"translations");
		if (translations.length() > 1) {
			Log.w(TAG, "Found " + translations.length() + " translations");
		}
		return translations.getJSONObject(0).getString("translatedText");
	}
}
