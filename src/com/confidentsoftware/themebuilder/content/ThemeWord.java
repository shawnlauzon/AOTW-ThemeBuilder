package com.confidentsoftware.themebuilder.content;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Tables.Secondary.THEMES_WORDS)
public class ThemeWord {
	@DatabaseField(generatedId = true) int id;
	@DatabaseField(foreign = true) Theme theme;
	@DatabaseField(foreign = true) Word word;
	
	ThemeWord() {
		// for ORMLite
	}
	
	public ThemeWord(Theme theme, Word word) {
		this.theme = theme;
		this.word = word;
	}
}
