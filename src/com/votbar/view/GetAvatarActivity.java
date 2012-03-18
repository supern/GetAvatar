package com.votbar.view;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.votbar.view.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author sanping.li@alipay.com 获取头像Activity
 *
 */
public class GetAvatarActivity extends Activity implements OnClickListener {
	public static final String ACTION_GET_AVATAR = "com.alipay.action.GET_AVATAR";

	public static final String EXTRA_INPUT = "input-type";
	public static final String EXTRA_DATA = "data-url";

	public static final int PHOTO_PICK = 0;
	public static final int CAPTURE = 1;

	private static final String IMAGE_UNSPECIFIED = "image/*";
	private static final String AVATAR_TMP_PATH = "__tmp_avatar.jpg";

	private CropImageView mCropImageView;
	private Button mBtnOk;
	private Button mBtnCancle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crop_image);

		Bundle extras = getIntent().getExtras();
		int input = extras.getInt(EXTRA_INPUT);

		loadAllVariables();

		Intent intent = null;
		switch (input) {
		case PHOTO_PICK:
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType(IMAGE_UNSPECIFIED);
			startActivityForResult(intent, PHOTO_PICK);
			break;
		case CAPTURE:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), AVATAR_TMP_PATH)));
//			intent.putExtra("return-data", true);
			startActivityForResult(intent, CAPTURE);
			break;

		default:
			break;
		}
	}

	private void loadAllVariables() {
		mBtnOk = (Button) findViewById(R.id.select);
		mBtnOk.setOnClickListener(this);
		mBtnCancle = (Button) findViewById(R.id.cancle);
		mBtnCancle.setOnClickListener(this);

		mCropImageView = (CropImageView) findViewById(R.id.crop);
	}

	private String writeBitmap(Bitmap bitmap) throws Exception {
		File imgFile = getFileStreamPath(AVATAR_TMP_PATH);
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(imgFile));
		bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
		bos.flush();
		bos.close();
		return imgFile.getPath();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCropImageView.recycle();
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnOk) {
			try {
				Bitmap bitmap = mCropImageView.getCropBitmap();
				String url = writeBitmap(bitmap);
				Intent intent = new Intent();
				intent.putExtra(EXTRA_DATA, url);
				setResult(RESULT_OK, intent);
				finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTO_PICK:
				Uri uri = data.getData();
				mCropImageView.setBitmap(getRealPathFromURI(uri));
				break;
			case CAPTURE:
				mCropImageView.setBitmap(Uri.fromFile(
						new File(Environment.getExternalStorageDirectory(),
								AVATAR_TMP_PATH)).getPath());
				break;
			default:
				break;
			}
		}else{
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		if (cursor == null)
			return null;

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

}
