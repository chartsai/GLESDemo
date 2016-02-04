package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles10.MaxwellTriangle;

public class GLES1_Maxwell_Triangle_Activity extends Activity {

    private TextView mColorValue;
    private boolean showAsHex = false;

    private int mStatusBarHeight;
    private int mPickX, mPickY;
    private boolean picked;

    private int mGLViewHight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles1_maxwell_triangle);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mColorValue = (TextView) findViewById(R.id.colorValueTextView);

        CheckBox cb = (CheckBox) findViewById(R.id.hexCheckBox);
        cb.setChecked(false);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showAsHex = isChecked;
                // used to trigger color text refresh.
                picked = true;
            }
        });

        mStatusBarHeight = getStatusBarHeight();
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int touchX = (int) (event.getX() + 0.5f);
                int touchY = (int) (event.getY() + 0.5f);

                // The origin point of GL is left-bottom, but the origin point of
                // touch point is left-top, thus we need to adjust the value of y-axis.
                // Also adjust the offset caused by status bar.
                mPickX = touchX;
                mPickY = (mGLViewHight - (touchY - mStatusBarHeight));

                picked = true;
                break;
            default:
                super.onTouchEvent(event);
        }

        return super.onTouchEvent(event);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        private MaxwellTriangle mTriangle;

        private ByteBuffer mPickedColorBuffer;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mTriangle = new MaxwellTriangle();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            mPickedColorBuffer = ByteBuffer.allocate(4);
            mPickedColorBuffer.order(ByteOrder.nativeOrder());
            mPickedColorBuffer.position(0);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            mGLViewHight = height;

            final float factor = 0.45f;

            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio * factor, ratio * factor, -1 * factor, 1 * factor, 3, 7);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            GLU.gluLookAt(gl, 0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            mTriangle.draw(gl);

            if (picked) {
                gl.glReadPixels(mPickX, mPickY, 1, 1, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, mPickedColorBuffer);
                byte[] pickedColors = mPickedColorBuffer.array();

                int r = pickedColors[0];
                int g = pickedColors[1];
                int b = pickedColors[2];
                int a = pickedColors[3];

                r = r >= 0? r: 256 + r;
                g = g >= 0? g: 256 + g;
                b = b >= 0? b: 256 + b;
                a = a >= 0? a: 256 + a;

                updateColorText(r, g, b, a);

                picked = false;
            }
        }
    }

    private void updateColorText(final int r, final int g, final int b, final int a) {
        // GL is running in Another Thread, so call UI thread to update TextView
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (showAsHex) {
                    String format = "color = (0x%2s, 0x%2s, 0x%2s, 0x%2s)";
                    mColorValue.setText(String.format(format,
                            Integer.toHexString(r), Integer.toHexString(g),
                            Integer.toHexString(b), Integer.toHexString(a)));
                } else {
                    String format = "color = (%3d, %3d, %3d, %3d)";
                    mColorValue.setText(String.format(format, r, g, b, a));
                }
            }
        });
    }
}
