package com.confidentsoftware.themebuilder;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.confidentsoftware.themebuilder.Dictionary.Images;
import com.confidentsoftware.themebuilder.ImageGallery.OnImageSelectedListener;
import com.confidentsoftware.themebuilder.ThemeGallery.OnThemeSelectedListener;
import com.confidentsoftware.themebuilder.WordList.OnWordSelectedListener;

public class ThemeBuilder extends Activity implements OnThemeSelectedListener,
		OnWordSelectedListener, OnImageSelectedListener {

	private static final String TAG = "ThemeBuilder";

	private static final String[] PROJECTION_IMAGE_DETAILS = { Images.URL,
			Images.NUM_LIKES };
	private static final int COLUMN_URL = 0;
	private static final int COLUMN_NUM_LIKES = 1;
	private static final String CACHE_DIR = "images/medium";

	private static final String DIALOG_ARG_URI = "uri";

	private WordList mWordList;
	private ImageGallery mImageGallery;

	private File mCacheDir;

	private ImageView mDialogImageView;
	private ToggleButton mDialogLikeButton;

	private Uri mDisplayedImageUri;
	private int mDisplayedNumLikes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mWordList = (WordList) getFragmentManager().findFragmentById(
				R.id.word_list);
		mImageGallery = (ImageGallery) getFragmentManager().findFragmentById(
				R.id.image_gallery);

		FileCache.getInstance().onCreate(this);

		// TODO extract this out to a separate class
		mCacheDir = new File(this.getCacheDir(), CACHE_DIR);
		mCacheDir.mkdirs();
	}

	@Override
	public void onThemeSelected(Uri themeUri) {
		mWordList.setTheme(themeUri);
		mImageGallery.clear();
	}

	@Override
	public void onLoadWordList(String filename) {
		mWordList.loadWordList(filename);
	}

	@Override
	public void onWordSelected(Uri wordUri) {
		mImageGallery.showImagesForWord(wordUri);
	}

	@Override
	public void onImageSelected(Uri imageUri) {
		Bundle args = new Bundle();
		args.putParcelable(DIALOG_ARG_URI, imageUri);
		showDialog(0, args);
		// FragmentTransaction ft = getFragmentManager().beginTransaction();
		// Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		// if (prev != null) {
		// ft.remove(prev);
		// }
		// ft.addToBackStack(null);
		//
		// // Create and show the dialog.
		// DialogFragment newFragment = ImageDialog.newInstance(imageUri);
		// newFragment.show(ft, "dialog");
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.image_view);
		mDialogImageView = (ImageView) dialog.findViewById(R.id.image_full);
		mDialogLikeButton = (ToggleButton) dialog
				.findViewById(R.id.like_button);
		mDialogLikeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((ToggleButton) v).isChecked()) {
					++mDisplayedNumLikes;
				} else {
					--mDisplayedNumLikes;
				}
				ContentValues values = new ContentValues();
				values.put(Images.NUM_LIKES, mDisplayedNumLikes);
				getContentResolver()
						.update(mDisplayedImageUri, values, null, null);
			}
		});
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		mDisplayedImageUri = args.getParcelable(DIALOG_ARG_URI);
		mDialogImageView.setImageDrawable(null);
		Cursor c = getContentResolver().query(mDisplayedImageUri,
				PROJECTION_IMAGE_DETAILS, null, null, null);
		if (c.moveToNext()) {
			String url = c.getString(COLUMN_URL);
			mDisplayedNumLikes = c.getInt(COLUMN_NUM_LIKES);
			Log.d(TAG, mDisplayedNumLikes + " people like this");
			mDialogLikeButton.setChecked(mDisplayedNumLikes > 0);
			new RetrieveImageTask(FileCache.TYPE_IMAGE, mDialogImageView)
					.execute(mDisplayedImageUri.getLastPathSegment(), url);
		}
	}

	/*
	 * public static class ImageDialog extends DialogFragment { int mNum;
	 * 
	 * static ImageDialog newInstance(Uri uri) { ImageDialog f = new
	 * ImageDialog();
	 * 
	 * Bundle args = new Bundle(); args.putParcelable("uri", uri);
	 * f.setArguments(args);
	 * 
	 * return f; }
	 * 
	 * @Override public void onCreate(Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState); Uri uri =
	 * getArguments().getParcelable("uri"); }
	 * 
	 * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
	 * container, Bundle savedInstanceState) { View v =
	 * inflater.inflate(R.layout.image_view, container, false); ImageView image
	 * = (ImageView) v.findViewById(R.id.image);
	 * 
	 * // Watch for button clicks. ToggleButton button = (ToggleButton)
	 * v.findViewById(R.id.like_button); button.setOnClickListener(new
	 * OnClickListener() { public void onClick(View v) { // When button is
	 * clicked, call up to owning activity. ((ThemeBuilder)
	 * getActivity()).showDialog(); } });
	 * 
	 * return v; } }
	 */
}
