package com.confidentsoftware.themebuilder.content;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * An image retrieved from some source. All images are currently retrieved from
 * Flickr, and the id is the id Flickr provides. Images may be retrieved from
 * other sources, as long as care is taken to keep ids unique.
 * 
 * @author slauzon
 * 
 */
@DatabaseTable(tableName = Tables.IMAGES) public class Image {

	public static final String SITE_FIELD_NAME = "site";
	public static final String IMAGE_ID_FIELD_NAME = "imageId";

	@DatabaseField(generatedId = true, columnName = BaseColumns._ID) int id;

	@DatabaseField(foreign = true, uniqueIndexName = "site_imageid_idx", columnName = SITE_FIELD_NAME) Site site;
	@DatabaseField(uniqueIndexName = "site_imageid_idx", columnName = IMAGE_ID_FIELD_NAME) String imageId;

	@DatabaseField(canBeNull = false) String url;
	@DatabaseField String thumbUrl;

	@DatabaseField int width;
	@DatabaseField int height;
	@DatabaseField int numFavorites;

	@DatabaseField(foreign = true) Attribution attribution;

	Image() {
		// for ORMLite
	}

	public Image(String url) {
		this.url = url;
	}
	
	public Image(String url, Site site, String imageId) {
		this(url);
		this.site = site;
		this.imageId = imageId;
	}
	
	public void setDimensions(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public void setNumFavorites(int numFavorites) {
		this.numFavorites = numFavorites;
	}
	
	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}
}
