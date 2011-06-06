package com.confidentsoftware.themebuilder;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {

	public static JSONObject getJsonFromUrl(String url) throws JSONException {
		String rawData = null;
		try {
			rawData = NetworkClient.downloadString(url);
		} catch (IOException ioe) {
			// wait 5 seconds and try again
			try {
				System.out.println(ioe.toString() + "; wait 5 seconds");
				Thread.sleep(5000);
				rawData = NetworkClient.downloadString(url);
			} catch (InterruptedException ie) {
				// ignore
			} catch (IOException ioe2) {
				// wait 10 seconds
				try {
					System.out.println(ioe.toString() + "; wait 10 seconds");
					Thread.sleep(10000);
					rawData = NetworkClient.downloadString(url);
				} catch (InterruptedException ie) {
					// ignore
				} catch (IOException ioe3) {
					// die
					throw new RuntimeException("Could not connect to server");
				}
			}
		}
		return new JSONObject(rawData.substring(rawData.indexOf("{"), rawData
				.lastIndexOf("}") + 1));
	}
}
