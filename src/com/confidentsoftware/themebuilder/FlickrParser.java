package com.confidentsoftware.themebuilder;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.confidentsoftware.themebuilder.Dictionary.Images;
import com.confidentsoftware.themebuilder.Dictionary.Sites;
import com.confidentsoftware.themebuilder.content.Author;
import com.confidentsoftware.themebuilder.content.DatabaseHelper;
import com.confidentsoftware.themebuilder.content.License;
import com.confidentsoftware.themebuilder.content.Site;
import com.j256.ormlite.dao.Dao;

public class FlickrParser {

	private static final String TAG = "FlickrParser";

	public static final int MAX_TITLE_LENGTH = 100;

	public static final float IDEAL_WIDTH_TO_HEIGHT_RATIO = 1.5f;
	public static final float WIDTH_TO_HEIGHT_LEEWAY = 0.3f;

	private static final String FLICKR_KEY = "6979b6bdf4a8d2615da04f43bbba2c95";
	@SuppressWarnings("unused")
	private static final String FLICKR_SECRET = "cb747e2dd9560db9";

	@SuppressWarnings("unused")
	private static final String AUTH_URL = "http://www.flickr.com/auth-72157626336966458";

	private static final String BASE_URL = "http://api.flickr.com/services/rest/?format=json&api_key="
			+ FLICKR_KEY;

	private static final String PHOTO_URL = "url_m";
	private static final String PHOTO_HEIGHT = "height_m";
	private static final String PHOTO_WIDTH = "width_m";
	private static final String THUMB_URL = "url_t";

	private static final String QUERY_GET_PERSON_INFO = BASE_URL
			+ "&method=flickr.people.getInfo&user_id=";
	private static final String QUERY_GET_FAVORITES = BASE_URL
			+ "&method=flickr.photos.getFavorites&photo_id=";

	private static final String QUERY_SEARCH_PHOTOS = BASE_URL
			+ "&method=flickr.photos.search&license=4,5,6,7,8&content_type=1&media=photos&per_page=500&extras=license,"
			+ PHOTO_URL + "," + THUMB_URL;

	private DatabaseHelper dbHelper;
	private Site flickrSite;

	private ContentResolver mCr;

	private static final String[] PROJECTION_ID = { BaseColumns._ID };

	public FlickrParser(Context context, DatabaseHelper helper)
			throws SQLException {
		this.dbHelper = helper;
		mCr = context.getContentResolver();
	}

	public void getImagesForWord(long wordId, String text) {
		Log.d(TAG, "*** Searching for images of " + text);
		int numImages = 0;
		for (String group : FlickrGroups.PREFERRED_ANIMAL_GROUPS) {
			numImages += addImagesForWord(wordId, text, group);
		}
		for (String group : FlickrGroups.PREFERRED_NATURE_GROUPS) {
			numImages += addImagesForWord(wordId, text, group);
		}
		for (String group : FlickrGroups.PREFERRED_GROUPS) {
			numImages += addImagesForWord(wordId, text, group);
		}

		if (numImages < 100) {
			Log.d(TAG, "Searching entire site");
			// Look through entire site
			numImages += addImagesForWord(wordId, text, null);
		}

		if (numImages < 100) {
			Log.w(TAG, "WARNING: Accepted only " + numImages + " images");
		} else {
			Log.i(TAG, "Accepted " + numImages + " images");
		}
	}

	private int addImagesForWord(long wordId, String text, String group) {

		StringBuilder method = new StringBuilder(QUERY_SEARCH_PHOTOS).append(
				"&tags=").append(buildTags(text));

		if (group != null) {
			method.append("&group_id=").append(group);
		}

		int numImages = 0;
		try {
			Log.d(TAG, method.toString());
			JSONObject queryResult = JsonParser.getJsonFromUrl(method
					.toString());
			queryResult = queryResult.getJSONObject("photos");
			int numPages = queryResult.getInt("pages");
			int numPhotos = queryResult.getInt("total");
			if (numPhotos > 1) {
				int curPage = 0;
				do {
					try {
						numImages = addImagesFromJson(wordId, text, queryResult);
						if (++curPage < numPages) {
							queryResult = JsonParser.getJsonFromUrl(method
									+ "&page=" + curPage);
							queryResult = queryResult.getJSONObject("photos");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} while (curPage < numPages);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return numImages;
	}

	/**
	 * Return the set of tags which would best match this word. Currently
	 * returns a single and plural version of the word, without articles.
	 * 
	 * @return
	 */
	private static String buildTags(String text) {
		StringBuilder tags = new StringBuilder();
		String singular = text;
		if (text.startsWith("a ")) {
			singular = text.substring(2);
		} else if (text.startsWith("an ")) {
			singular = text.substring(3);
		}
		tags.append(singular).append(",").append(singular).append("s");
		return tags.toString();
	}

	private int addImagesFromJson(long wordId, String text,
			JSONObject queryResult) throws JSONException {
		JSONArray photos = queryResult.getJSONArray("photo");
		int numImages = 0;
		for (int i = 0; i < photos.length(); i++) {
			JSONObject photo = null;
			try {
				photo = photos.getJSONObject(i);
			} catch (JSONException e) {
				continue;
			}
			try {
				if (isPhotoOk(photo)) {
					String url = photo.getString(PHOTO_URL);
					String title = photo.getString("title");
					if (title.length() > MAX_TITLE_LENGTH) {
						title = title.substring(0, MAX_TITLE_LENGTH - 3)
								+ "...";
					}
					String id = photo.getString("id");
					Log.d(TAG, "Image: " + id);
					Uri imageUri = Images.findImage(mCr,
							Sites.CONTENT_URI_FLICKR, id);
					if (imageUri != null) {
						Log.d(TAG, "Found existing image: " + imageUri);
						// Just insert the mapping
						Uri mappingUri = Dictionary.Words.CONTENT_URI
								.buildUpon().appendPath(String.valueOf(wordId))
								.appendPath(Dictionary.PATH_IMAGES).appendPath(
										imageUri.getLastPathSegment()).build();
						Cursor c = mCr.query(mappingUri, PROJECTION_ID, null,
								null, null);
						try {
							if (!c.moveToNext()) {
								mCr.insert(mappingUri, null);
							}
						} finally {
							c.close();
						}
					} else {
						ContentValues values = new ContentValues();
						values.put(Images.URL, url);
						values.put(Images.SITE, ContentUris
								.parseId(Sites.CONTENT_URI_FLICKR));
						values.put(Images.SITE_UNIQUE_NAME, id);
						values.put(Images.WIDTH, photo.getInt(PHOTO_WIDTH));
						values.put(Images.HEIGHT, photo.getInt(PHOTO_HEIGHT));
						values
								.put(Images.THUMB_URL, photo
										.getString(THUMB_URL));
						values.put(Images.NUM_FAVORITES, getNumFavorites(id));
						imageUri = mCr
								.insert(Dictionary.BASE_CONTENT_URI.buildUpon()
										.appendPath(Dictionary.PATH_WORDS)
										.appendPath(String.valueOf(wordId))
										.appendPath(Dictionary.PATH_IMAGES)
										.build(), values);
						//
						// String ownerName = photo.getString("owner");
						// Author author = findAuthor(ownerName);
						// if (author == null) {
						// author = buildAuthor(ownerName);
						// }
						// TODO set license
						// image.setLicense(License.getLicenseByFlickrId(photo
						// .getInt("license")));
						// dbHelper.getImageDao().create(image);
					}
					// dbHelper.getWordImageDao().create(
					// new WordImage(word, image));
					numImages++;
				}
			} catch (JSONException e) {
				// Catch here so that we continue through the loop
				Log.w(TAG, "Error parsing photo: " + photo, e);
			}
		}
		return numImages;
	}

	private Author findAuthor(String authorId) throws SQLException {
		Dao<Author, Integer> authorDao = dbHelper.getAuthorDao();
		Map<String, Object> fieldValues = new HashMap<String, Object>(2);
		fieldValues.put(Author.SITE_FIELD_NAME, flickrSite);
		fieldValues.put(Author.AUTHOR_ID_FIELD_NAME, authorId);
		List<Author> authors = authorDao.queryForFieldValues(fieldValues);
		Author author = null;
		if (authors.size() > 0) {
			author = authors.get(0);
		}
		return author;
	}

	private License findLicense(String authorId) throws SQLException {
		// Dao<License, Integer> authorDao = dbHelper.getAuthorDao();
		// QueryBuilder<License, Integer> qb = authorDao.queryBuilder();
		// Where<License, Integer> where = qb.where();
		// where.eq(Author.SITE_FIELD_NAME, flickrSite).and().eq(
		// Author.AUTHOR_ID_FIELD_NAME, authorId);
		// PreparedQuery<Author> query = qb.prepare();
		// return authorDao.queryForFirst(query);
		return null;
	}

	private boolean isPhotoOk(JSONObject photo) throws JSONException {

		boolean result = true;
		float width = photo.getInt(PHOTO_WIDTH);
		float height = photo.getInt(PHOTO_HEIGHT);
		float ratio = width / height;
		// System.out.println("Ratio: " + ratio);
		if (ratio > IDEAL_WIDTH_TO_HEIGHT_RATIO * (1 + WIDTH_TO_HEIGHT_LEEWAY)
				|| ratio < IDEAL_WIDTH_TO_HEIGHT_RATIO
						* (1 - WIDTH_TO_HEIGHT_LEEWAY)) {
			result = false;
		}
		// System.out.println("Accepting size " + width + "x" + height);

		return result;
	}

	private Author buildAuthor(String authorId) throws SQLException,
			JSONException {
		String method = QUERY_GET_PERSON_INFO + authorId;
		JSONObject queryResult = JsonParser.getJsonFromUrl(method);
		queryResult = queryResult.getJSONObject("person");
		String username = queryResult.getJSONObject("username").getString(
				"_content");
		Author author = new Author(username, flickrSite, authorId);
		if (queryResult.has("realname")) {
			author.setFullName(queryResult.getJSONObject("realname").getString(
					"_content"));
		}
		if (queryResult.has("profileurl")) {
			author.setProfileUrl(queryResult.getJSONObject("profileurl")
					.getString("_content"));
		}
		if (queryResult.has("mobileurl")) {
			author.setMobileUrl(queryResult.getJSONObject("mobileurl")
					.getString("_content"));
		}
		dbHelper.getAuthorDao().create(author);
		return author;
	}

	private int getNumFavorites(String imageId) throws JSONException {
		String method = QUERY_GET_FAVORITES + imageId;
		JSONObject queryResult = JsonParser.getJsonFromUrl(method);
		queryResult = queryResult.getJSONObject("photo");
		return queryResult.getInt("total");
	}
}
