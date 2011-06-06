package com.confidentsoftware.themebuilder.content;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String TAG = "DatabaseHelper";

	private static final String DATABASE_NAME = "full_dictionary.db";
	private static final int DATABASE_VERSION = 6;

	private Dao<Language, String> languageDao;
	private Dao<Site, Integer> siteDao;
	private Dao<Theme, Integer> themeDao;
	private Dao<Word, Integer> wordDao;
	private Dao<ThemeWord, Void> themeWordDao;
	private Dao<Image, Integer> imageDao;
	private Dao<WordImage, Void> wordImageDao;
	private Dao<Author, Integer> authorDao;
	private Dao<License, Integer> licenseDao;
	private Dao<Attribution, Integer> attributionDao;
	private Dao<Audio, Integer> audioDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// clearTables(getConnectionSource());
	}

	public Dao<Language, String> getLanguageDao() throws SQLException {
		if (languageDao == null) {
			languageDao = getDao(Language.class);
		}
		return languageDao;
	}

	public Dao<Theme, Integer> getThemeDao() throws SQLException {
		if (themeDao == null) {
			themeDao = getDao(Theme.class);
		}
		return themeDao;
	}

	public Dao<Word, Integer> getWordDao() throws SQLException {
		if (wordDao == null) {
			wordDao = getDao(Word.class);
		}
		return wordDao;
	}

	public Dao<ThemeWord, Void> getThemeWordDao() throws SQLException {
		if (themeWordDao == null) {
			themeWordDao = getDao(ThemeWord.class);
		}
		return themeWordDao;
	}

	public Dao<WordImage, Void> getWordImageDao() throws SQLException {
		if (wordImageDao == null) {
			wordImageDao = getDao(WordImage.class);
		}
		return wordImageDao;
	}

	public Dao<Image, Integer> getImageDao() throws SQLException {
		if (imageDao == null) {
			imageDao = getDao(Image.class);
		}
		return imageDao;
	}

	public Dao<Author, Integer> getAuthorDao() throws SQLException {
		if (authorDao == null) {
			authorDao = getDao(Author.class);
		}
		return authorDao;
	}

	public Dao<License, Integer> getLicenseDao() throws SQLException {
		if (licenseDao == null) {
			licenseDao = getDao(License.class);
		}
		return licenseDao;
	}

	public Dao<Attribution, Integer> getAttributionDao() throws SQLException {
		if (attributionDao == null) {
			attributionDao = getDao(Attribution.class);
		}
		return attributionDao;
	}

	public Dao<Audio, Integer> getAudioDao() throws SQLException {
		if (audioDao == null) {
			audioDao = getDao(Audio.class);
		}
		return audioDao;
	}

	public Dao<Site, Integer> getSiteDao() throws SQLException {
		if (siteDao == null) {
			siteDao = getDao(Site.class);
		}
		return siteDao;
	}
	
	@Override
	public void onCreate(SQLiteDatabase database,
			ConnectionSource connectionSource) {
		Log.i(TAG, "onCreate");
		try {
			TableUtils.createTable(connectionSource, Attribution.class);
			TableUtils.createTable(connectionSource, Audio.class);
			TableUtils.createTable(connectionSource, Author.class);
			TableUtils.createTable(connectionSource, Image.class);
			TableUtils.createTable(connectionSource, Language.class);
			TableUtils.createTable(connectionSource, License.class);
			TableUtils.createTable(connectionSource, Site.class);
			TableUtils.createTable(connectionSource, Theme.class);
			TableUtils.createTable(connectionSource, ThemeWord.class);
			TableUtils.createTable(connectionSource, Word.class);
			TableUtils.createTable(connectionSource, WordImage.class);
		} catch (SQLException e) {
			Log.d(TAG, "Cannot create database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database,
			ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(TAG, "onUpgrade");
			dropTables(connectionSource);
			onCreate(database, connectionSource);
		} catch (SQLException e) {
			Log.e(TAG, "Cannot drop databases", e);
			throw new RuntimeException(e);
		}
	}

	private void clearTables(ConnectionSource connectionSource) {
		try {
			TableUtils.clearTable(connectionSource, Attribution.class);
			TableUtils.clearTable(connectionSource, Audio.class);
			TableUtils.clearTable(connectionSource, Author.class);
			TableUtils.clearTable(connectionSource, Image.class);
			TableUtils.clearTable(connectionSource, Language.class);
			TableUtils.clearTable(connectionSource, License.class);
			TableUtils.clearTable(connectionSource, Site.class);
			TableUtils.clearTable(connectionSource, Theme.class);
			TableUtils.clearTable(connectionSource, ThemeWord.class);
			TableUtils.clearTable(connectionSource, Word.class);
			TableUtils.clearTable(connectionSource, WordImage.class);
		} catch (SQLException e) {
			Log.d(TAG, "Cannot clear database", e);
			throw new RuntimeException(e);
		}
	}

	private void dropTables(ConnectionSource connectionSource)
			throws SQLException {
		TableUtils.dropTable(connectionSource, Attribution.class, true);
		TableUtils.dropTable(connectionSource, Audio.class, true);
		TableUtils.dropTable(connectionSource, Author.class, true);
		TableUtils.dropTable(connectionSource, Image.class, true);
		TableUtils.dropTable(connectionSource, Language.class, true);
		TableUtils.dropTable(connectionSource, License.class, true);
		TableUtils.dropTable(connectionSource, Site.class, true);
		TableUtils.dropTable(connectionSource, Theme.class, true);
		TableUtils.dropTable(connectionSource, ThemeWord.class, true);
		TableUtils.dropTable(connectionSource, Word.class, true);
		TableUtils.dropTable(connectionSource, WordImage.class, true);
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		siteDao = null;
	}
}
