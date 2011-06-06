package com.confidentsoftware.themebuilder.content;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "attributions") public class Attribution {

	@DatabaseField(generatedId = true, columnName = BaseColumns._ID) int id;
	@DatabaseField(foreign = true) Author author;
	@DatabaseField String title;
	@DatabaseField String url;
	@DatabaseField(foreign = true) License license;
	@DatabaseField String copyrightText;
	
	Attribution() {
		// for ORMLote
	}
	
	public Attribution(License license) {
		this.license = license;
	}
}
