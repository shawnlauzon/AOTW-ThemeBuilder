package com.confidentsoftware.themebuilder;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public interface Dictionary {

	public static final String AUTHORITY = "com.confidentsoftware.allofthewords";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY);

	public static final String PATH_THEMES = "themes";
	public static final String PATH_WORDS = "words";
	public static final String PATH_IMAGES = "images";
	public static final String PATH_LANGUAGES = "languages";

	static final String[] PROJECTION_ID = new String[] { BaseColumns._ID };

	public class Themes implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				BASE_CONTENT_URI, "themes");

		public static final String NAME = "name";
	}

	public class Sites implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				BASE_CONTENT_URI, "sites");

		public static final Uri CONTENT_URI_FLICKR = ContentUris
				.withAppendedId(CONTENT_URI, 1);
		public static final Uri CONTENT_URI_SHTOOKA = ContentUris
				.withAppendedId(CONTENT_URI, 2);
	}

	public class Words implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				BASE_CONTENT_URI, "words");

		public static final String TEXT = "text";
		public static final String LANGUAGE = "language_id";
	}
	
	public class Languages implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				BASE_CONTENT_URI, "languages");

		public static final String NAME = "langId";
	}
	

	public class Images implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				BASE_CONTENT_URI, "images");

		public static final String URL = "url";
		public static final String THUMB_URL = "thumbUrl";
		public static final String SITE = "site";
		public static final String SITE_UNIQUE_NAME = "imageId";
		public static final String NUM_FAVORITES = "numFavorites";
		public static final String WIDTH = "width";
		public static final String HEIGHT = "height";

		public static final String NUM_LIKES = "numRatings";

		private static String SELECTION__UNIQUE = SITE + " = ? AND "
				+ SITE_UNIQUE_NAME + " = ?";

		/**
		 * Find and return an image with a unique name, as defined by the source
		 * site. If one does not exist, return null.
		 */
		public static Uri findImage(ContentResolver cr, Uri siteUri,
				String siteUniqueName) {

			Uri imageUri = null;
			String[] selectionArgs = new String[] {
					siteUri.getLastPathSegment(), siteUniqueName };

			Cursor c = cr.query(CONTENT_URI, PROJECTION_ID, SELECTION__UNIQUE,
					selectionArgs, null);
			try {
				if (c.moveToNext()) {
					imageUri = ContentUris.withAppendedId(CONTENT_URI, c
							.getLong(0));
				}
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return imageUri;
		}
	}

	public class Secondary {
		public class ThemesWords implements BaseColumns {
			public static final String THEME_ID = "theme_id";
			public static final String WORD_ID = "word_id";
		}

		public class WordsImages implements BaseColumns {
			public static final String WORD_ID = "word_id";
			public static final String IMAGE_ID = "image_id";
		}
	}
}
