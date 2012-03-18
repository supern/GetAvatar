package com.votbar.view;

import com.votbar.view.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class Main extends Activity{
	private static final int PHOTOCROP = 0;
	
	private AvatarHelper mAvatarHelper;

	private Button button;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mAvatarHelper = new AvatarHelper(this);
		
		button = (Button) findViewById(R.id.get_image);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAvatarHelper.createSelector(PHOTOCROP);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTOCROP:
				ImageView imageView = (ImageView) findViewById(R.id.image);
				imageView.setImageBitmap(mAvatarHelper.getAvatar(data));
				break;

			default:
				break;
			}
		}
	}


}