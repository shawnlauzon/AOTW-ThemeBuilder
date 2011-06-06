package com.confidentsoftware.themebuilder.content;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "sites")
public class Site {

	@DatabaseField(id = true, columnName = BaseColumns._ID) int id;
	@DatabaseField(canBeNull = false) String name;

	Site() {
		// for ORMLite
	}

	public Site(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
