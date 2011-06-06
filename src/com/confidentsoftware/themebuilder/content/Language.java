package com.confidentsoftware.themebuilder.content;

import java.util.HashMap;
import java.util.Map;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "languages") public class Language {

	public static final Language ENGLISH = new Language("en", "English",
			"English");
	public static final Language FRENCH = new Language("fr", "French",
			"Français");
	public static final Language SPANISH = new Language("es", "Spanish",
			"Español");
	public static final Language PORTUGUESE = new Language("pt", "Portuguese",
			"Português");
	public static final Language ITALIAN = new Language("it", "Italian",
			"Italiano");

	private static final Map<String, Language> languageMap;

	static {
		languageMap = new HashMap<String, Language>();
		languageMap.put("en", ENGLISH);
		languageMap.put("fr", FRENCH);
		languageMap.put("es", SPANISH);
		languageMap.put("pt", PORTUGUESE);
		languageMap.put("it", ITALIAN);
	}

	@DatabaseField(generatedId = true, columnName = BaseColumns._ID) int id;
	@DatabaseField(uniqueIndex = true) String langId;
	@DatabaseField(canBeNull = false) String languageEn;
	@DatabaseField(canBeNull = false) String languageNative;

	Language() {
		// For ORMLite
	}

	public Language(String id, String languageEn, String languageNative) {
		this.langId = id;
		this.languageEn = languageEn;
		this.languageNative = languageNative;
	}

	public static Language findLanguage(String id) {
		return languageMap.get(id);
	}
}
