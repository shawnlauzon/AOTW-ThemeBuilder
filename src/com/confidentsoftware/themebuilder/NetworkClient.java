package com.confidentsoftware.themebuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.net.http.AndroidHttpClient;

public class NetworkClient {

	private static final String TAG = "NetworkClient";
	
	private static HttpClient client = AndroidHttpClient.newInstance("ThemeBuilder");

	public static InputStream downloadData(String url) throws IOException {

		InputStream is = null;
		HttpGet get = new HttpGet(url);

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (ClientProtocolException cpe) {
			cpe.printStackTrace();
		} catch (IOException ioe) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}

		return is;
	}

	public static String downloadString(String url) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(
				downloadData(url)));
		StringBuilder sb = null;
		try {
			sb = new StringBuilder();
			String line = r.readLine();
			while (line != null) {
				sb.append(line);
				line = r.readLine();
			}
		} finally {
			r.close();
		}
		String result = null;
		if (sb != null) {
			result = sb.toString();
		}
		return result;
	}

	public static void downloadFile(String url, File file) throws IOException {
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();

		BufferedOutputStream os = new BufferedOutputStream(
				new FileOutputStream(file));
		try {
			entity.writeTo(os);
		} finally {
			os.close();
		}
	}
}
