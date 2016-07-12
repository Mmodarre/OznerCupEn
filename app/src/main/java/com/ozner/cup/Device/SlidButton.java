package com.ozner.cup.Device;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.ozner.cup.R;

/**
 * Created by mengdongya on 2015/12/2.
 */
public class SlidButton extends View implements OnTouchListener {

    private boolean nowChoose = false;
    private boolean onSlip = false;
    private float downX, nowX;
    private Rect btn_on, btn_off;

    private boolean isChgLsnOn = false;
    private OnChangedListener changedLis;

    int begin, end;

    private Bitmap bg_on, bg_off, slip_btn;

    public SlidButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlidButton(Context context) {
        super(context);
        init();
    }


    private void init() {
        bg_on = BitmapFactory.decodeResource(getResources(),
                R.drawable.sild_bg_on);
        bg_off = BitmapFactory.decodeResource(getResources(),
                R.drawable.sild_bg_off);
        slip_btn = BitmapFactory.decodeResource(getResources(),
                R.drawable.sild_bg_btn);
        btn_on = new Rect(0, 0, slip_btn.getWidth(), slip_btn.getHeight());

        btn_off = new Rect(bg_off.getWidth() - slip_btn.getWidth(), 0,
                bg_off.getWidth(), slip_btn.getHeight());
        setOnTouchListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Matrix matrix = new Matrix();
        begin = this.getWidth() - bg_off.getWidth();
        end = this.getHeight() / 4;
        matrix.postTranslate(begin, end);
        Paint paint = new Paint();
        float x = 0;

        {
            if (!nowChoose) {
                canvas.drawBitmap(bg_off, matrix, paint);
            } else {
                canvas.drawBitmap(bg_on, matrix, paint);

            }

            if (onSlip) {
                if (nowX >= bg_on.getWidth())
                    x = bg_on.getWidth() - slip_btn.getWidth() / 2;
                else
                    x = nowX - slip_btn.getWidth() / 2;
            } else {
                if (nowChoose)
                    x = btn_off.left;
                else
                    x = btn_on.left;
            }

            if (x < 0)
                x = 0;
            else if (x > bg_on.getWidth() - slip_btn.getWidth())
                x = bg_on.getWidth() - slip_btn.getWidth();

            canvas.drawBitmap(slip_btn, x + begin, end, paint);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE:
                nowX = event.getX();
                break;
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < begin)
                    return false;
                onSlip = true;
                downX = event.getX();
                nowX = downX;
                break;
            case MotionEvent.ACTION_UP:
                onSlip = false;
                boolean lastChoose = nowChoose;

                nowChoose = !nowChoose;
                if (isChgLsnOn && (lastChoose != nowChoose))
                    changedLis.OnChanged(nowChoose);
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }


    public void SetOnChangedListener(OnChangedListener l) {
        isChgLsnOn = true;
        changedLis = l;
    }

    public void SetSlidButtonState(boolean bool) {
        this.nowChoose = bool;
    }

    public interface OnChangedListener {
        void OnChanged(boolean checkState);
    }

    protected boolean getSlidButtonState() {
        return nowChoose;
    }


}   