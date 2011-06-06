package com.confidentsoftware.themebuilder.content;

import java.util.HashMap;
import java.util.Map;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "licenses") public class License {

	public static final int CC_BY_20_ID = 4;
	public static final int CC_BY_SA_20_ID = 5;
	public static final int CC_BY_ND_20_ID = 6;
	public static final int CC_PD = 7;
	public static final int US_GOV = 8;
	public static final int CC_BY_30_ID = 10;
	
	private static Map<Integer, License> mIdToLicenseMap = new HashMap<Integer, License>();;
	private static Map<String, License> mSourceNameToLicenseMap = new HashMap<String, License>();;

	static {
		License lic = new License(4,
				"Creative Commons Attribution 2.0 Generic", "CC BY 2.0",
				"cc_by", "http://creativecommons.org/licenses/by/2.0/");
		storeLicense(lic, "Creative Commons BY");
		lic = new License(5,
				"Creative Commons Attribution-ShareAlike 2.0 Generic",
				"CC BY-SA 2.0", "cc_by_sa",
				"http://creativecommons.org/licenses/by-sa/2.0/");
		storeLicense(lic, "Creative Commons BY-SA");
		lic = new License(6,
				"Creative Commons Attribution-NoDerivs 2.0 Generic",
				"CC BY-ND 2.0", "cc_by_nd",
				"http://creativecommons.org/licenses/by-nd/2.0/");
		lic = new License(7, "No known copyright restrictions", "CC PD",
				"cc_zero", "http://www.flickr.com/commons/usage/");
		lic = new License(8, "United States Government Work", "US GOV",
				"us_gov", "http://www.usa.gov/copyright.shtml");
		lic = new License(10, "Creative Commons Attribution 3.0 United States",
				"CC BY 3.0", "cc_by",
				"http://creativecommons.org/licenses/by/3.0/us/");
		storeLicense(lic, "Creative Commons BY 3.0 (U.S)");
		storeLicense(lic, "Creative Commons BY 3.0 U.S");
	}

	@DatabaseField(id = true, columnName = BaseColumns._ID) int id;
	@DatabaseField(canBeNull = false) String name;
	@DatabaseField(canBeNull = false) String abbreviation;
	@DatabaseField(canBeNull = false) String licenseDrawableName;
	@DatabaseField(canBeNull = false) String url;

	License() {
		// for ORMLite
	}

	private License(int id, String name, String abbreviation,
			String licenseDrawableName, String url) {
		this.id = id;
		this.name = name;
		this.abbreviation = abbreviation;
		this.licenseDrawableName = licenseDrawableName;
		this.url = url;
		storeLicense(this);
	}

	private static void storeLicense(License license) {
		mIdToLicenseMap.put(license.id, license);
	}

	private static void storeLicense(License license, String sourceName) {
		storeLicense(license);
		mSourceNameToLicenseMap.put(sourceName, license);
	}

	public static License getLicenseByFlickrId(int id) {
		License lic = mIdToLicenseMap.get(id);
		if (lic == null) {
			throw new IllegalArgumentException("Could not find license " + id);
		}
		return lic;
	}

	/**
	 * Used by data sources such as the Shtooka database where the names are
	 * variable.
	 */
	public static License getLicenseBySourceName(String name) {
		License lic = mSourceNameToLicenseMap.get(name);
		if (lic == null) {
			throw new IllegalArgumentException("Could not find license " + name);
		}
		return lic;
	}
}
