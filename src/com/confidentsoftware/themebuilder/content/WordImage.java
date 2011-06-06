package com.confidentsoftware.themebuilder.content;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Tables.Secondary.WORDS_IMAGES) public class WordImage {

	@DatabaseField(generatedId = true) int id;
	@DatabaseField(foreign = true) Word word;
	@DatabaseField(foreign = true) Image image;

	@DatabaseField int numRatings;
	@DatabaseField int totalRating;

	WordImage() {
		// for ORMLite
	}

	public WordImage(Word word, Image image) {
		this.word = word;
		this.image = image;
	}
}
