package com.confidentsoftware.themebuilder.content;

import android.content.ContentValues;

import com.confidentsoftware.themebuilder.Dictionary.Themes;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Tables.THEMES)
public class Theme {

	@DatabaseField(generatedId = true, columnName = Themes._ID)
	int id;
	@DatabaseField(canBeNull = false, columnName = Themes.NAME)
	String name;

	Theme() {
		// for ORMLite use
	}

	public Theme(String name) {
		this.name = name;
	}
}
