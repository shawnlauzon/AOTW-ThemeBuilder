package com.confidentsoftware.themebuilder.content;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "authors") public class Author {

	public static final String SITE_FIELD_NAME = "site";
	public static final String AUTHOR_ID_FIELD_NAME = "authorId";

	@DatabaseField(generatedId = true, columnName = BaseColumns._ID) int id;

	@DatabaseField(foreign = true, uniqueIndexName = "site_userid_idx", columnName = SITE_FIELD_NAME) Site site;
	@DatabaseField(uniqueIndexName = "site_userid_idx", columnName = AUTHOR_ID_FIELD_NAME) String userId;

	@DatabaseField(canBeNull = false) String screenName;
	@DatabaseField String fullName;
	@DatabaseField String profileUrl;
	@DatabaseField String mobileUrl;

	Author() {
		// for ORMLIte
	}
	
	public Author(String screenName) {
		this.screenName = screenName;
	}
	
	public Author(String screenName, Site site, String userId) {
		this(screenName);
		this.site = site;
		this.userId = userId;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public void setMobileUrl(String mobileUrl) {
		this.mobileUrl = mobileUrl;
	}
}
