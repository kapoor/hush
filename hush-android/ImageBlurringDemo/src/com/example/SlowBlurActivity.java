package com.example;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;

public class SlowBlurActivity extends Activity {
    private ImageView mImage;
    private TextView mText;

    private OnPreDrawListener mPreDrawListener = new OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            ViewTreeObserver observer = mText.getViewTreeObserver();
            if(observer != null) {
                observer.removeOnPreDrawListener(this);
            }
            Drawable drawable = mImage.getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap != null) {
                    blur(bitmap, mText, 25);
                }
            }
            return true;
        }
    };

    private OnGlobalLayoutListener mLayoutListener = new OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {
            ViewTreeObserver observer = mText.getViewTreeObserver();
            if(observer != null) {
                observer.addOnPreDrawListener(mPreDrawListener);
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mImage = (ImageView) findViewById(R.id.image);
        mText = (TextView)findViewById(R.id.text);
        if (mImage != null && mText != null) {
            ViewTreeObserver observer = mText.getViewTreeObserver();
            if (observer != null) {
                observer.addOnGlobalLayoutListener(
                        mLayoutListener);
            }
        }
    }

    private void blur(Bitmap bkg, View view, float radius) {

        Bitmap overlay = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(overlay);
        canvas.drawBitmap(bkg, -view.getLeft(), -view.getTop(), null);

        RenderScript rs = RenderScript.create(this);

        Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());

        blur.setInput(overlayAlloc);

        blur.setRadius(radius);

        blur.forEach(overlayAlloc);

        overlayAlloc.copyTo(overlay);

        view.setBackground(new BitmapDrawable(getResources(), overlay));

        rs.destroy();
    }

}
