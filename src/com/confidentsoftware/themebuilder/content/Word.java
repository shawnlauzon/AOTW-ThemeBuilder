package com.confidentsoftware.themebuilder.content;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Tables.WORDS)
public class Word {

	@DatabaseField(generatedId = true, columnName = BaseColumns._ID) int id;

	@DatabaseField(canBeNull = false, foreign = true) Language language;
	@DatabaseField(canBeNull = false) String text;
	@DatabaseField(foreign = true) Audio audio;
	
	Word() {
		// for ORMLite
	}

	public Word(Language language, String text) {
		this.language = language;
		this.text = text;
	}
	
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "Word [id=" + id + ", text=" + text + "]";
	}

	public Image getImageForLanguage(Language lang) {
		Image img = null;
//		for (WordImage wi : mappings) {
//			if (wi.word.language.equals(lang)) {
//				img = wi.image;
//			}
//		}
		return img;
	}

}
