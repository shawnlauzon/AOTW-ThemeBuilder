package com.confidentsoftware.themebuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.confidentsoftware.themebuilder.Dictionary.Words;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class WordList extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener,
		OnClickListener {

	private static final String TAG = "WordList";

	private static String[] PROJECTION_WORDS = new String[] { Words._ID,
			Words.TEXT };
	private static String[] PROJECTION_LANGUAGES = new String[] { Words.LANGUAGE };

	private static final int WORDS_LOADER_ID = 0;
	private static final int LANGUAGES_LOADER_ID = 1;

	private EditText mNewLanguageField;
	private Spinner mLanguageChooser;

	private OnWordSelectedListener mListener;
	private SimpleCursorAdapter mWordsAdapter;
	private ArrayAdapter<String> mLanguagesAdapter;
	private AsyncTask<?, ?, ?> mAsyncTask;

	private Uri mThemeUri;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.word_list, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnWordSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnWordSelectedListener");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();
		mNewLanguageField = (EditText) activity.findViewById(R.id.new_language);
		mLanguageChooser = (Spinner) activity
				.findViewById(R.id.current_language);
		mLanguageChooser.setOnItemSelectedListener(new OnLanguageChangedListener());
		activity.findViewById(R.id.translate_button).setOnClickListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		OpenHelperManager.releaseHelper();
		if (mAsyncTask != null) {
			mAsyncTask.cancel(true);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.translate_button:
			String newLanguage = mNewLanguageField.getText().toString();
			mNewLanguageField.setText("");
			// FIXME This should add the new language to the spinner and select
			// it, but it doesn't
			mLanguagesAdapter.add(newLanguage);
			mLanguagesAdapter.notifyDataSetChanged();
			mLanguageChooser.setSelection(mLanguageChooser.getCount() - 1);
			new TranslatorTask(getActivity().getContentResolver(), mThemeUri,
					newLanguage).execute();
			loadLanguages();
		}
	}

	public void setTheme(Uri themeUri) {
		mThemeUri = themeUri;
		loadLanguages();
	}

	private void loadLanguages() {
		getLoaderManager().restartLoader(LANGUAGES_LOADER_ID, null, this);
	}

	private void loadWords() {
		mWordsAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_list_item_2, null, PROJECTION_WORDS,
				new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		setListAdapter(mWordsAdapter);
		getListView().setOnItemClickListener(this);

		getLoaderManager().restartLoader(WORDS_LOADER_ID, null, this);
	}
	
	public void loadWordList(String filename) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(getResources()
					.getAssets().open(filename)));
			mAsyncTask = new LoadWordsTask(getActivity(), in, mThemeUri)
					.execute((Void) null);
		} catch (IOException e) {
			Log.e(TAG, "Could not read file animals_en.txt", e);
			handleException(e);
		} catch (SQLException e) {
			Log.e(TAG, "Insert into database failed", e);
			handleException(e);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> loader = null;
		Uri uri;
		switch (id) {
		case WORDS_LOADER_ID:
			uri = Uri.withAppendedPath(mThemeUri, Dictionary.PATH_WORDS);
			Log.d(TAG, "Creating loader: " + uri);
			loader = new CursorLoader(getActivity(), uri, PROJECTION_WORDS,
					Words.LANGUAGE + " = ?", new String[] { getSelectedLanguage()  },
					Words.TEXT);
			break;
		case LANGUAGES_LOADER_ID:
			uri = Uri.withAppendedPath(mThemeUri, Dictionary.PATH_LANGUAGES);
			Log.d(TAG, "Creating loader: " + uri);
			loader = new CursorLoader(getActivity(), uri, PROJECTION_LANGUAGES,
					null, null, Words.LANGUAGE);
			break;
		}
		return loader;
	}
	
	private String getSelectedLanguage() {
		String language;
		if (mLanguageChooser.getSelectedView() == null) {
			Log.w(TAG, "Could not get language; using 'en'");
			language = "en";
		} else {
			language = ((TextView) mLanguageChooser.getSelectedView()).getText().toString();
		}
		return language;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO It's here we should clear the old data, not during reload
		switch (loader.getId()) {
		case WORDS_LOADER_ID:
			mWordsAdapter.swapCursor(data);
			break;
		case LANGUAGES_LOADER_ID:
			setLanguagesAdapter(data);
			mLanguageChooser.setSelection(0);
			loadWords();
			break;
		}
		Log.d(TAG, "Load finished");
	}
	
	private void setLanguagesAdapter(Cursor data) {
		List<String> languages = new ArrayList<String>();
		while (data.moveToNext()) {
			languages.add(data.getString(0));
		}
		mLanguagesAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, languages);
		mLanguagesAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLanguageChooser.setAdapter(mLanguagesAdapter);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case WORDS_LOADER_ID:
			mWordsAdapter.swapCursor(null);
			break;
		case LANGUAGES_LOADER_ID:
			// do nothing; not using cursor
			break;
		}
	}

	private void handleException(Exception e) {
		throw new RuntimeException(e);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mListener.onWordSelected(ContentUris.withAppendedId(Words.CONTENT_URI,
				id));
	}

	/**
	 * Implemented by the Activity to handle events generated by this fragment.
	 * 
	 * @author slauzon
	 * 
	 */
	public interface OnWordSelectedListener {
		public void onWordSelected(Uri wordUri);
	}
	
	private class OnLanguageChangedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			loadWords();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
		}
	}
}
