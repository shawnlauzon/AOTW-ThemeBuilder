package com.confidentsoftware.themebuilder.content;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.confidentsoftware.themebuilder.Dictionary;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class ThemeProvider extends ContentProvider implements Dictionary {

	private static final String TAG = "ThemeProvider";

	public static final String WORD_ID = "word_id";
	public static final String WORD_TEXT = "text";
	public static final String NUM_FAVORITES = "numFavorites";
	public static final String THUMB_URL = "thumbUrl";

	private static final int IMAGES = 1;
	private static final int IMAGES_ID = 2;
	private static final int WORDS = 3;
	private static final int WORDS_ID = 4;
	private static final int THEMES = 5;
	private static final int THEMES_ID = 6;
	private static final int THEMES_WORDS = 7;
	private static final int THEMES_WORDS_ID = 8;
	private static final int THEMES_WORDS_LINK_ID = 9;
	private static final int THEMES_LANGUAGES = 10;

	private static final int WORDS_IMAGES = 100;
	private static final int WORDS_IMAGES_ID = 101;
	private static final int WORDS_IMAGES_LINK_ID = 102;
	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI(AUTHORITY, "images", IMAGES);
		sUriMatcher.addURI(AUTHORITY, "images/#", IMAGES_ID);
		sUriMatcher.addURI(AUTHORITY, "words", WORDS);
		sUriMatcher.addURI(AUTHORITY, "words/#", WORDS_ID);
		sUriMatcher.addURI(AUTHORITY, "themes", THEMES);
		sUriMatcher.addURI(AUTHORITY, "themes/#", THEMES_ID);
		sUriMatcher.addURI(AUTHORITY, "themes/#/words", THEMES_WORDS);
		sUriMatcher.addURI(AUTHORITY, "themes/#/words/#", THEMES_WORDS_ID);
		sUriMatcher.addURI(AUTHORITY, "themes/#/words/#/#",
				THEMES_WORDS_LINK_ID);
		sUriMatcher.addURI(AUTHORITY, "themes/#/languages", THEMES_LANGUAGES);
		sUriMatcher.addURI(AUTHORITY, "words/#/images", WORDS_IMAGES);
		sUriMatcher.addURI(AUTHORITY, "words/#/images/#", WORDS_IMAGES_ID);
		sUriMatcher.addURI(AUTHORITY, "words/#/images/#/#",
				WORDS_IMAGES_LINK_ID);
	}

	private static final String THEMES_WORDS_JOIN_TABLES = Tables.WORDS
			+ " INNER JOIN " + Tables.Secondary.THEMES_WORDS + " ON "
			+ Tables.THEMES + "." + Dictionary.Themes._ID + " = "
			+ Dictionary.Secondary.ThemesWords.THEME_ID + " INNER JOIN "
			+ Tables.THEMES + " ON " + Tables.WORDS + "."
			+ Dictionary.Themes._ID + " = "
			+ Dictionary.Secondary.ThemesWords.WORD_ID;

	private static final String WORDS_IMAGES_JOIN_TABLES = Tables.IMAGES
			+ " INNER JOIN " + Tables.Secondary.WORDS_IMAGES + " ON "
			+ Tables.WORDS + "." + Dictionary.Words._ID + " = "
			+ Dictionary.Secondary.WordsImages.WORD_ID + " INNER JOIN "
			+ Tables.WORDS + " ON " + Tables.IMAGES + "."
			+ Dictionary.Images._ID + " = "
			+ Dictionary.Secondary.WordsImages.IMAGE_ID;

	private DatabaseHelper mDbHelper;

	@Override
	public boolean onCreate() {
		mDbHelper = (DatabaseHelper) OpenHelperManager.getHelper(getContext(),
				DatabaseHelper.class);
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert=" + uri + ", values=" + values);

		Uri newUri = null;
		int match = sUriMatcher.match(uri);
		switch (match) {
		case THEMES:
			newUri = insertAndNotify(uri, Tables.THEMES, values);
			break;
		case WORDS:
			newUri = insertAndNotify(uri, Tables.WORDS, values);
			break;
		case THEMES_WORDS:
			newUri = insertAndNotify(uri, Tables.WORDS, values);
			if (newUri != null) {
				// newUri = THEMES_WORDS_ID
				insert(newUri, null);
			}
			break;
		case IMAGES:
			newUri = insertAndNotify(uri, Tables.IMAGES, values);
			break;
		case WORDS_IMAGES:
			newUri = insertAndNotify(uri, Tables.IMAGES, values);
			if (newUri != null) {
				// newUri = WORDS_IMAGES_ID
				insert(newUri, null);
			}
			break;
		case THEMES_WORDS_ID:
		case WORDS_IMAGES_ID:
			newUri = insertLinkTableAndNotify(uri);
			break;
		default:
			Log.w(TAG, "Did not find URI matcher");
		}

		Log.d(TAG, "Inserted Uri " + newUri);
		return newUri;
	}

	private Uri insertAndNotify(Uri uri, String table, ContentValues values) {
		Uri newUri = null;
		long rowId = insertValues(table, values);
		if (rowId >= 0) {
			newUri = ContentUris.withAppendedId(uri, rowId);
			notifyChange(newUri);
		}
		return newUri;
	}

	private long insertValues(String table, ContentValues values) {
		return mDbHelper.getWritableDatabase().insert(table, null, values);
	}

	private void notifyChange(Uri uri) {
		Log.d(TAG, "notifyChange: " + uri);
		getContext().getContentResolver().notifyChange(uri, null);
	}

	private Uri insertLinkTableAndNotify(Uri uri) {
		Log.d(TAG, "insertLinkTableAndNotify: " + uri);
		Uri newUri = null;
		String table = null;
		String column1 = null;
		String column2 = null;
		int match = sUriMatcher.match(uri);
		switch (match) {
		case THEMES_WORDS_ID:
			// just built a Word
			table = Tables.Secondary.THEMES_WORDS;
			column1 = Dictionary.Secondary.ThemesWords.THEME_ID;
			column2 = Dictionary.Secondary.ThemesWords.WORD_ID;
			break;
		case WORDS_IMAGES_ID:
			// just built an Image
			table = Tables.Secondary.WORDS_IMAGES;
			column1 = Dictionary.Secondary.WordsImages.WORD_ID;
			column2 = Dictionary.Secondary.WordsImages.IMAGE_ID;
			break;
		}
		List<String> pathSegments = uri.getPathSegments();
		long id1 = Long.parseLong(pathSegments.get(1));
		long id3 = Long.parseLong(pathSegments.get(3));
		ContentValues values = new ContentValues();
		values.put(column1, id1);
		values.put(column2, id3);
		Log.d(TAG, "Inserting link table " + values);
		long rowId = mDbHelper.getWritableDatabase()
				.insert(table, null, values);
		if (rowId >= 0) {
			newUri = ContentUris.withAppendedId(uri, rowId);
			notifyChange(newUri);
		}
		return newUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "query=" + uri + ", projection="
				+ Arrays.toString(projection) + ", selection=" + selection
				+ ", selectionArgs=" + Arrays.toString(selectionArgs)
				+ ", sortOrder=" + sortOrder);

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		qBuilder.setTables(getTablesForUri(uri));
		qBuilder.setProjectionMap(getProjectionMapForUri(uri, projection));
		String where = getAddionalWhereClausesForUri(uri);
		qBuilder.setDistinct(isDistinct(uri));
		if (where != null) {
			qBuilder.appendWhere(where);
		}
		if (sortOrder != null) {
			sortOrder = disambiguateSortOrder(uri, sortOrder);
		}

		Log.d(TAG, "Doing query: "
				+ qBuilder.buildQuery(projection, selection, null, // groupBy,
						null, // having,
						sortOrder, null));

		// Make the query.
		Cursor c = qBuilder.query(db, projection, selection, selectionArgs,
				null, // groupBy,
				null, // having,
				sortOrder);
		Log.d(TAG, "Found " + c.getCount() + " rows");
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	private static String getTablesForUri(Uri queryUri) {
		String tables = null;
		int match = sUriMatcher.match(queryUri);
		switch (match) {
		case THEMES:
		case THEMES_ID:
			tables = Tables.THEMES;
			break;
		case IMAGES:
		case IMAGES_ID:
			tables = Tables.IMAGES;
			break;
		case WORDS:
		case WORDS_ID:
			tables = Tables.WORDS;
			break;
		case THEMES_WORDS:
		case THEMES_WORDS_ID:
			tables = THEMES_WORDS_JOIN_TABLES;
			break;
		case WORDS_IMAGES:
		case WORDS_IMAGES_ID:
			tables = WORDS_IMAGES_JOIN_TABLES;
			break;
		case THEMES_LANGUAGES:
			tables = THEMES_WORDS_JOIN_TABLES;
			break;
		}
		Log.d(TAG, "Tables: " + tables);
		return tables;
	}

	/**
	 * Build a projection map to disambiguate the _ID columns which are the same
	 * in every table. We only need to do this where there's more than one table
	 */
	private static Map<String, String> getProjectionMapForUri(Uri queryUri,
			String[] projection) {
		Map<String, String> projectionMap = null;
		int match = sUriMatcher.match(queryUri);
		switch (match) {
		case THEMES_WORDS:
		case THEMES_WORDS_ID:
		case THEMES_LANGUAGES:
			projectionMap = addTableToId(Tables.WORDS, projection);
			break;
		case WORDS_IMAGES:
		case WORDS_IMAGES_ID:
			projectionMap = addTableToId(Tables.IMAGES, projection);
			break;
		}
		Log.d(TAG, "ProjectionMap: " + projectionMap);
		return projectionMap;
	}

	private static Map<String, String> addTableToId(String tableName,
			String[] projection) {
		Map<String, String> projectionMap = new HashMap<String, String>(
				projection.length);
		for (String s : projection) {
			if (s.equals(BaseColumns._ID)) {
				projectionMap.put(s, tableName + "." + BaseColumns._ID);
			} else {
				projectionMap.put(s, s);
			}
		}
		return projectionMap;
	}

	private static String getAddionalWhereClausesForUri(Uri uri) {
		String where = null;
		int match = sUriMatcher.match(uri);
		switch (match) {
		case THEMES_ID:
			where = addIdEquality(Tables.THEMES, uri, 1);
			break;
		case IMAGES_ID:
			where = addIdEquality(Tables.IMAGES, uri, 1);
			break;
		case WORDS_ID:
			where = addIdEquality(Tables.WORDS, uri, 1);
			break;
		case THEMES_WORDS:
			where = addIdEquality(Tables.THEMES, uri, 1);
			break;
		case THEMES_WORDS_ID:
			where = new StringBuilder(addIdEquality(Tables.THEMES, uri, 1))
					.append(" AND ")
					.append(addIdEquality(Tables.WORDS, uri, 3)).toString();
			break;
		case WORDS_IMAGES:
			where = addIdEquality(Tables.WORDS, uri, 1);
			break;
		case WORDS_IMAGES_ID:
			where = new StringBuilder(addIdEquality(Tables.WORDS, uri, 1))
					.append(" AND ").append(
							addIdEquality(Tables.IMAGES, uri, 3)).toString();
			break;
		case THEMES_LANGUAGES:
			where = addIdEquality(Tables.THEMES, uri, 1);
			break;
		}
		Log.d(TAG, "Additional where: " + where);
		return where;
	}

	private static String addIdEquality(String tableName, Uri uri, int position) {
		return new StringBuilder(tableName).append(".").append(BaseColumns._ID)
				.append(" = ").append(uri.getPathSegments().get(position))
				.toString();
	}

	private static String disambiguateSortOrder(Uri queryUri, String sortOrder) {
		if (sortOrder.startsWith(BaseColumns._ID)) {
			String tables = null;
			int match = sUriMatcher.match(queryUri);
			switch (match) {
			case THEMES_WORDS:
			case THEMES_WORDS_ID:
				tables = Tables.WORDS;
				break;
			case WORDS_IMAGES:
			case WORDS_IMAGES_ID:
				tables = Tables.IMAGES;
				break;
			}

			if (tables != null) {
				sortOrder = tables + "." + sortOrder;
			}
		}

		Log.d(TAG, "sort order: " + sortOrder);
		return sortOrder;
	}

	private static boolean isDistinct(Uri uri) {
		return sUriMatcher.match(uri) == THEMES_LANGUAGES;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.d(TAG, "update=" + uri + ", values=" + values);

		int numRows = 0;
		int match = sUriMatcher.match(uri);
		switch (match) {
		case WORDS_IMAGES_ID:
			// FIXME don't hard-code and do better string concatenation support
			// null selection
			if (selection == null) {
				selection = "word_id = " + uri.getPathSegments().get(1)
						+ " AND image_id=" + uri.getPathSegments().get(3);
			} else {
				selection = "word_id = " + uri.getPathSegments().get(1)
						+ " AND image_id=" + uri.getPathSegments().get(3)
						+ " AND " + selection;
			}

			numRows = updateAndNotify(uri, Tables.Secondary.WORDS_IMAGES,
					values, selection, selectionArgs);
			break;
		default:
			Log.w(TAG, "Did not find URI matcher");
		}

		return numRows;
	}

	private int updateAndNotify(Uri uri, String table, ContentValues values,
			String selection, String[] whereArgs) {
		int rows = updateValues(table, values, selection, whereArgs);
		// FIXME Not sure how to notify all these
		// if (rows >= 0) {
		// newUri = ContentUris.withAppendedId(uri, rowId);
		// notifyChange(newUri);
		// }
		return rows;
	}

	private int updateValues(String table, ContentValues values,
			String whereClause, String[] whereArgs) {
		return mDbHelper.getWritableDatabase().update(table, values,
				whereClause, whereArgs);
	}
}
