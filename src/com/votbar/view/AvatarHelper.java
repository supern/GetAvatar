/**
 * 
 */
package com.votbar.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author sanping.li@alipay.com 获取头像辅助类
 * 
 */
public class AvatarHelper implements OnClickListener {
	private Activity mActivity;
	private int mRequestCode;

	public AvatarHelper(Activity activity) {
		mActivity = activity;
	}

	public void createSelector(int requestCode) {
		mRequestCode = requestCode;
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.dialog_title_set_avatar);
		builder.setItems(new String[] { mActivity.getString(R.string.dialog_menu_capture), mActivity.getString(R.string.dialog_menu_pick) }, this);
		builder.setNegativeButton(R.string.cancle, null);
		builder.show();
	}

	public Bitmap getAvatar(Intent data) {
		String url = data.getStringExtra(GetAvatarActivity.EXTRA_DATA);
		Options options = new Options();
		options.inDensity = getDensityDpi();
		options.inScaled = true;
		return BitmapFactory.decodeFile(url, options);
	}

	private int getDensityDpi() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		Display display = ((WindowManager) mActivity
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		display.getMetrics(displayMetrics);
		return displayMetrics.densityDpi;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Intent intent = new Intent(GetAvatarActivity.ACTION_GET_AVATAR);
		switch (which) {
		case 0:
			intent.putExtra(GetAvatarActivity.EXTRA_INPUT,
					GetAvatarActivity.CAPTURE);
			break;
		case 1:
			intent.putExtra(GetAvatarActivity.EXTRA_INPUT,
					GetAvatarActivity.PHOTO_PICK);
			break;
		default:
			break;
		}
		mActivity.startActivityForResult(intent, mRequestCode);

	}
}
