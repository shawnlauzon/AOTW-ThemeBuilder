package com.confidentsoftware.themebuilder;

public class QueryStmts {

	public static final String QUERY_IMAGES = "SELECT images.id, numFavorites, word, licenses.id, images.url FROM images, words, word_image, licenses WHERE images.id = word_image.image_id AND word_image.word_id = words.id AND images.license_id = licenses.id ORDER BY words.word asc, numFavorites desc";
}
