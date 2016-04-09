package idv.chatea.gldemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles20.PixelatedTexture;

public class GLES2_Pixelated_Picture_Activity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_PICTURE = 100;

    private GLSurfaceView mGLSurface;
    private MyRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles2_pixelated_picture);

        mGLSurface = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        mGLSurface.setEGLContextClientVersion(2);
        mRenderer = new MyRenderer();
        mGLSurface.setRenderer(mRenderer);
        mGLSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        Button selectPictureButton = (Button) findViewById(R.id.selectPictureButton);
        selectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_SELECT_PICTURE);
            }
        });

        SeekBar selectDensity = (SeekBar) findViewById(R.id.densitySeekBar);
        selectDensity.setMax(100);
        selectDensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                if (fromUser) {
                    mGLSurface.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mRenderer.setDensity(progress);
                            mGLSurface.requestRender();
                        }
                    });
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        selectDensity.setProgress(100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_PICTURE && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Get image failed...", Toast.LENGTH_SHORT).show();
                return;
            }
            final Uri pictureUri = data.getData();
            mGLSurface.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRenderer.setPictureUri(pictureUri);
                }
            });
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {
        private float[] mProjectMatrix = new float[16];
        private float[] mViewMatrix = new float[16];

        private PixelatedTexture mTexture;

        private Uri mCurrentPictureUri = null;

        public void setPictureUri(Uri uri) {
            try {
                /**
                 * Set current picture uri to avoid the EGLContext has not been created.
                 * If the EGLContext not exist, mTextrue.setPicture(bitmap) will failed.
                 * And {@link #onSurfaceChanged} will try to setup the picture again.
                 */
                mCurrentPictureUri = uri;
                if (((EGL10) EGLContext.getEGL()).eglGetCurrentContext().equals(EGL10.EGL_NO_CONTEXT)) {
                    // Try to set picture uri before EGLContext exist, abort.
                    return;
                }
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                mTexture.setPicture(bitmap);
                mGLSurface.requestRender();
                bitmap.recycle();
            } catch (Exception e) {
                Toast.makeText(GLES2_Pixelated_Picture_Activity.this,
                        "Cannot open the image file", Toast.LENGTH_SHORT).show();
            }
        }

        public void setDensity(int density) {
            mTexture.setDensity(density);
        }

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.android_logo);
            mTexture = new PixelatedTexture(bitmap);
            bitmap.recycle();
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            if (mCurrentPictureUri != null) {
                setPictureUri(mCurrentPictureUri);
            }

            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 1.5f, 10);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            Matrix.setLookAtM(mViewMatrix, 0,
                    0f, 0f, 5f,
                    0f, 0f, 0f,
                    0f, 1f, 0);

            float[] mvpMatrix = new float[16];
            Matrix.multiplyMM(mvpMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

            float[] moduleMatrix = new float[16];
            Matrix.setIdentityM(moduleMatrix, 0);

            Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, moduleMatrix, 0);

            mTexture.draw(mvpMatrix);
        }
    }
}
