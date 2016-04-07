package idv.chatea.gldemo.firework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.R;

/**
 * Created by chatea on 2016/2/1.
 * @author Charlie Tsai (chatea)
 */
public class GLFireworkRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = GLFireworkRenderer.class.getSimpleName();

    private static final float PUFFIN_FIREWORK_SIZE_FACTOR = 1.5f;

    /**
     * keep the distance with firework to get better physical effect experience.
     */
    private static final float FACTOR_OF_GL_PROJECTION = 10f;

    private Context mContext;

    private float[] mViewportData;
    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];

    /**
     * This used to generate the color and alpha of firework.
     */
    private final Random mRandomGenerator = new Random();

    private final List<GLFirework> mFireworks;

    private GLBlurFirework.Shader mBlurShader;
    private GLImageFirework.Shader mFireworkImageShader;
    private GLPointFirework.Shader mPointShader;

    public GLFireworkRenderer(Context context) {
        mContext = context;

        mFireworks = new ArrayList<>();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);

        mBlurShader = new GLBlurFirework.Shader();

        Bitmap puffinBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.android_logo);
        mFireworkImageShader = new GLImageFirework.Shader(puffinBitmap);
        puffinBitmap.recycle();

        Bitmap pointBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.particle);
        mPointShader = new GLPointFirework.Shader(pointBitmap);
        pointBitmap.recycle();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        mViewportData = new float[] {0, 0, width, height};

        float ratio = (float) width / height;

        Matrix.frustumM(mProjectMatrix, 0,
                -ratio * FACTOR_OF_GL_PROJECTION, ratio * FACTOR_OF_GL_PROJECTION,
                -FACTOR_OF_GL_PROJECTION, FACTOR_OF_GL_PROJECTION,
                5 * FACTOR_OF_GL_PROJECTION, 15 * FACTOR_OF_GL_PROJECTION);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(mViewMatrix, 0,
                0f, 0f, FACTOR_OF_GL_PROJECTION * 10f,
                0f, 0f, 0f,
                0f, 1f, 0);

        float[] vpMatrix = new float[16];
        Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);


        synchronized (mFireworks) {
            // use iterator to avoid concurrent issue when removing entity.
            Iterator<GLFirework> iterator = mFireworks.iterator();
            while (iterator.hasNext()) {
                GLFirework firework = iterator.next();
                firework.render(vpMatrix);
                if (firework.isDead()) {
                    iterator.remove();
                }
            }
        }
    }

    public void onScreenTouched(float touchX, float touchY) {
        float[] pickedPoint = rayPick(touchX, touchY);
        Log.i(TAG, "picked Point = " + pickedPoint[0] + ", "
                + pickedPoint[1] + ", " + pickedPoint[2]);

        float[] newModuleMatrix = new float[16];
        Matrix.setIdentityM(newModuleMatrix, 0);
        Matrix.translateM(newModuleMatrix, 0, pickedPoint[0], pickedPoint[1], pickedPoint[2]);

        float x = pickedPoint[0];
        float y = pickedPoint[1];
        float z = pickedPoint[2];
        float thickness = 15 + 10 * mRandomGenerator.nextFloat();
        GLBlurFirework firework = new GLBlurFirework(180, x, y, z, 0.4f, 10, 35, thickness);

        // alpha of firework should in the range [0.75f, 1.0f)
        firework.setAlpha(0.75f + mRandomGenerator.nextFloat() / 4);

        // select major color randomly.
        float r, g, b;

        switch (mRandomGenerator.nextInt(4)) {
            case 0: // red
                r = 0.8f + mRandomGenerator.nextFloat() / 5;
                g = mRandomGenerator.nextFloat() / 2;
                b = mRandomGenerator.nextFloat() / 2;
                break;
            case 1: // green
                r = mRandomGenerator.nextFloat() / 2;
                g = 0.8f + mRandomGenerator.nextFloat() / 5;
                b = mRandomGenerator.nextFloat() / 2;
                break;
            case 2: // blue
                r = mRandomGenerator.nextFloat() / 2;
                g = mRandomGenerator.nextFloat() / 2;
                b = 0.8f + mRandomGenerator.nextFloat() / 5;
                break;
            case 3: // white
                r = 0.8f + mRandomGenerator.nextFloat() / 5;
                g = 0.8f + mRandomGenerator.nextFloat() / 5;
                b = 0.8f + mRandomGenerator.nextFloat() / 5;
                break;
            default:
                r = 0.0f;
                g = 0.0f;
                b = 0.0f;
                break;
        }

        firework.setColor(r, g, b, 1.0f);

        firework.bindShader(mBlurShader);

        // TODO consider use cached buffer.
        synchronized (mFireworks) {
            mFireworks.add(firework);
        }
    }

    /**
     * handle the double touched event.
     * @param x0 the screen coordinate of first point on x-axis
     * @param y0 the screen coordinate of second point on y-axis
     * @param x1 the screen coordinate of first point on x-axis
     * @param y1 the screen coordinate of second point on y-axis
     */
    public void onDoubleTouched(float x0, float y0, float x1, float y1) {
        // calculate the size first.
        float[] pickPoint0 = rayPick(x0, y0);
        float[] pickPoint1 = rayPick(x1, y1);

        float width = Math.abs(pickPoint0[0] - pickPoint1[0]) * PUFFIN_FIREWORK_SIZE_FACTOR;
        float height = Math.abs(pickPoint0[0] - pickPoint1[0]) * PUFFIN_FIREWORK_SIZE_FACTOR;

        // calculate the parameters
        float touchedCenterX = (x0 + x1) / 2;
        float touchedCenterY = (y0 + y1) / 2;

        float[] centerPoint = rayPick(touchedCenterX, touchedCenterY);
        Log.i(TAG, "picked Point = " + centerPoint[0] + ", "
                + centerPoint[1] + ", " + centerPoint[2]);

        float[] newModuleMatrix = new float[16];
        Matrix.setIdentityM(newModuleMatrix, 0);
        Matrix.translateM(newModuleMatrix, 0, centerPoint[0], centerPoint[1], centerPoint[2]);

        float x = centerPoint[0];
        float y = centerPoint[1];
        float z = centerPoint[2];
        float thickness = 2 + 0.1f * (float) mRandomGenerator.nextGaussian();
        int density = 150 + (int) (20 * mRandomGenerator.nextGaussian());
        GLImageFirework firework = new GLImageFirework(100, x, y, z, 2f, width, height, density, thickness);

        // Add a little rotation on puffin firework.
        float xAngle = (float) (2 * mRandomGenerator.nextGaussian());
        float yAngle = (float) (2 * mRandomGenerator.nextGaussian());
        float zAngle = (float) (4 * mRandomGenerator.nextGaussian());
        firework.setRotation(xAngle, yAngle, zAngle);

        // alpha of firework should in the range [0.75f, 1.0f)
        firework.setAlpha(0.9f + mRandomGenerator.nextFloat() / 10);

        firework.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        firework.bindShader(mFireworkImageShader);

        // TODO consider use cached buffer.
        synchronized (mFireworks) {
            mFireworks.add(firework);
        }
    }

    public void onDeviceShake(float speed) {
        // Let's bomb!!!!

        int newFireworkNumber = (int) speed / 100;
        ArrayList<GLFirework> tempList = new ArrayList<>(newFireworkNumber);
        for (int i = 0; i < newFireworkNumber; i++) {
            float x = mRandomGenerator.nextInt(30) - 15;
            float y = mRandomGenerator.nextInt(30) - 15;
            float thickness = (float) (10 + 4f * mRandomGenerator.nextGaussian());

            GLPointFirework firework = new GLPointFirework(60, x, y, 0, 20, 30, thickness);
            firework.setAlpha(1f);

            // Take more red color!
            firework.setColor(1.0f + mRandomGenerator.nextFloat() / 5,
                    mRandomGenerator.nextFloat(), mRandomGenerator.nextFloat(), 1.0f);
            firework.bindShader(mPointShader);
            firework.setDelay(i * 5);
            tempList.add(firework);
        }

        // TODO consider use cached buffer.
        synchronized (mFireworks) {
            mFireworks.addAll(tempList);
        }
    }

    /**
     * implement ray-pick to mapping the clicked point on screen to the world coordinate of GL.
     * @param touchX touched position X in screen coordinate
     * @param touchY touched position Y in screen coordinate
     * @return the picked point {x,y,z}
     */
    private float[] rayPick(float touchX, float touchY) {
        float screenW = mViewportData[2] - mViewportData[0];
        float screenH = mViewportData[3] - mViewportData[1];

        // Y axis of screen is inverted to GL, correct it.
        touchY = screenH - touchY;

        float[] normalizedVector = new float[4];
        normalizedVector[0] = touchX * 2 / screenW - 1;
        normalizedVector[1] = touchY * 2 / screenH - 1;
        normalizedVector[2] = 0f;
        normalizedVector[3] = 1f;

        float[] invertedMatrix = new float[16];
        float[] pickedPoint = new float[4];

        float[] vpMatrix = new float[16];
        Matrix.multiplyMM(vpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

        Matrix.invertM(invertedMatrix, 0, vpMatrix, 0);

        Matrix.multiplyMV(pickedPoint, 0, invertedMatrix, 0, normalizedVector, 0);

        return new float[] {
                pickedPoint[0] / pickedPoint[3],
                pickedPoint[1] / pickedPoint[3],
                pickedPoint[2] / pickedPoint[3]
        };
    }
}
