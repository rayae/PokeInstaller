package cn.bavelee.imagize;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private ImageView img;
    private float startDistance, endDistance;
    private int midX = 0, midY = 0;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        img.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (midX == 0 && midY == 0) {
            Point point = new Point();
            getWindowManager().getDefaultDisplay().getSize(point);
            midX = point.x / 2;
            midY = point.y / 2;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    startDistance = getDistance(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    endDistance = getDistance(event);
                    if (startDistance < endDistance) {
                        //判断缩小比例
                        float[] values = new float[9];
                        img.getImageMatrix().getValues(values);
                        float f = values[Matrix.MSCALE_X] * 1.1f;
                        if (f < 3f) {
                            img.getImageMatrix().postScale(1.1f, 1.1f, midX, midY);
                            img.invalidate();
                        }
                    } else {
                        //判断缩小比例
                        float[] values = new float[9];
                        img.getImageMatrix().getValues(values);
                        float f = values[Matrix.MSCALE_X] * 0.9f;
                        if (f > 0.5f) {
                            img.getImageMatrix().postScale(0.9f, 0.9f, midX, midY);
                            img.invalidate();
                        }
                    }
                }
                break;
        }
        return true;
    }

    private float getDistance(MotionEvent event) {
        float x = Math.abs(event.getX(0) - event.getX(1));
        float y = Math.abs(event.getY(0) - event.getY(1));
        return (float) Math.sqrt(x * x + y * y);
    }

    private void initView() {
        img = (ImageView) findViewById(R.id.img);
    }
}
