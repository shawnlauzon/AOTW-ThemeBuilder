package com.confidentsoftware.themebuilder.content;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "audio") public class Audio {
	@DatabaseField(generatedId = true, columnName = BaseColumns._ID) int id;

	@DatabaseField(canBeNull = false) String fileName;
	@DatabaseField(foreign = true) Attribution attribution;

	Audio() {
		// for ORMLIte
	}
	
	public Audio(String fileName) {
		this.fileName = fileName;
	}
}
