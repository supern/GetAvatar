/**
 * 
 */
package com.votbar.view;

import com.votbar.view.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/**
 *@author sanping.li@alipay.com 图片剪切组件
 * 
 */
public class CropImageView extends FrameLayout implements OnGestureListener,
		View.OnTouchListener, View.OnClickListener {
	private Bitmap mBitmap;
	private int mWidth;
	private int mHeight;
	private Matrix mMatrix;

	private int mClipWidth;
	private int mClipHeight;
	private RectF mMask;

	private GestureDetector mDetector;

	private DisplayMetrics mDisplayMetrics;

	public CropImageView(Context context) {
		super(context);
		getDisplay();

		init();
	}

	public CropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getDisplay();

		TypedArray a = getResources().obtainAttributes(attrs,
				R.styleable.CropImage);
		mClipWidth = (int) (a.getInteger(R.styleable.CropImage_width, 100) / mDisplayMetrics.density);
		mClipHeight = (int) (a.getInteger(R.styleable.CropImage_height, 100) / mDisplayMetrics.density);
		a.recycle();
		setMinimumWidth(mClipWidth);
		setMinimumHeight(mClipHeight);

		init();
	}

	private void getDisplay() {
		mDisplayMetrics = new DisplayMetrics();
		Display display = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE)).getDefaultDisplay();
		display.getMetrics(mDisplayMetrics);
	}

	private void init() {
		mDetector = new GestureDetector(this);
		setOnTouchListener(this);
		setLongClickable(true);

		View cropAction = LayoutInflater.from(getContext()).inflate(
				R.layout.crop_image_action, null);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		params.bottomMargin = 5;
		addView(cropAction, params);

		ImageButton button = (ImageButton) cropAction.findViewById(R.id.crop_rotate);
		button.setOnClickListener(this);
		button = (ImageButton) cropAction.findViewById(R.id.crop_zoomin);
		button.setOnClickListener(this);
		button.setHapticFeedbackEnabled(false);
		button = (ImageButton) cropAction.findViewById(R.id.crop_zoomout);
		button.setOnClickListener(this);
		button.setHapticFeedbackEnabled(false);
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
		mWidth = bitmap.getWidth();
		mHeight = bitmap.getHeight();
		requestLayout();
	}

	public void setBitmap(String path) {
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int dw = options.outWidth / mDisplayMetrics.widthPixels;
		int dh = options.outHeight / mDisplayMetrics.heightPixels;
		int scale = Math.max(dw, dh) * 4 / 5;
		scale = Math.max(scale, 1);

		options = new Options();
		options.inDensity = mDisplayMetrics.densityDpi;
		options.inScaled = true;
		options.inPurgeable = true;
		options.inSampleSize = scale;
		setBitmap(BitmapFactory.decodeFile(path, options));
	}

	private void initMatrix() {
		mMatrix = new Matrix();
		float scale = 1.0f;
		if (mWidth > mHeight) {
			scale = mDisplayMetrics.widthPixels * 1.0f / mWidth;
		} else {
			scale = mDisplayMetrics.heightPixels * 1.0f / mHeight;
		}
		mMatrix.postScale(scale, scale);

		float dx = mMask.centerX() - mWidth * scale * 0.5f;
		float dy = mMask.centerY() - mHeight * scale * 0.5f;
		mMatrix.postTranslate(dx, dy);
	}

	public void setCropSize(int w, int h) {
		mClipWidth = w;
		mClipHeight = h;
		requestLayout();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, mMatrix, null);
		}

		Paint paint = new Paint();
		paint.setColor(Color.YELLOW);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(4);

		canvas.clipRect(mMask, Region.Op.XOR);
		canvas.drawColor(0x99000000);
		canvas.drawRect(mMask, paint);
		super.dispatchDraw(canvas);

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			Rect rect = new Rect(0, 0, right - left, bottom - top);
			mMask = new RectF(rect.centerX() - mClipWidth * 0.5f,
					rect.centerY() - mClipHeight * 0.5f, rect.centerX()
							+ mClipWidth * 0.5f, rect.centerY() + mClipHeight
							* 0.5f);
			initMatrix();
		}
	}

	public void resizeBitmap(float scale) {
		RectF rect = getMapedRect();
		mMatrix.postScale(scale, scale, rect.centerX(), rect.centerY());
		postInvalidate();
	}

	public void rotateBitmap(float degree) {
		RectF rect = getMapedRect();
		mMatrix.postRotate(degree, rect.centerX(), rect.centerY());
		postInvalidate();
	}

	private RectF getMapedRect() {
		RectF rect = new RectF(0, 0, mWidth, mHeight);
		mMatrix.mapRect(rect);
		return rect;
	}

	public Bitmap getCropBitmap() {
		if (mBitmap == null)
			return null;
		Bitmap bitmap = Bitmap.createBitmap(mClipWidth, mClipHeight,
				Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.translate(-mMask.left, -mMask.top);
		canvas.drawBitmap(mBitmap, mMatrix, null);
		return bitmap;
	}

	public void recycle() {
		if (mBitmap != null)
			mBitmap.recycle();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		mMatrix.postTranslate(-distanceX, -distanceY);
		postInvalidate();
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mDetector.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.crop_rotate:
			rotateBitmap(90);
			break;
		case R.id.crop_zoomin:
			resizeBitmap(1.1f);
			break;
		case R.id.crop_zoomout:
			resizeBitmap(0.9f);
			break;
		default:
			break;
		}
	}
}
