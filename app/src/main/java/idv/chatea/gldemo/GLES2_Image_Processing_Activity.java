package idv.chatea.gldemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles20.RectangleTexture;
import idv.chatea.gldemo.postprocess.FrameBuffer;
import idv.chatea.gldemo.postprocess.Kernel;
import idv.chatea.gldemo.postprocess.KernelPostProcessing;

/**
 * Used to demo how using shader to do image processing.
 * This is a sample for kernel convolution.
 *
 * Please check the wiki: https://en.wikipedia.org/wiki/Kernel_(image_processing)
 */
public class GLES2_Image_Processing_Activity extends AppCompatActivity {
    private static final int REQUEST_CODE_SELECT_PICTURE = 100;

    private GLSurfaceView mGLSurfaceView;
    private MyRenderer mRenderer;

    private Spinner mEffectSelectorSpinner;
    private Button mSelectPictureButton;

    private static final String[] KERNEL_EFFECT_LIST = {
            "Identity",
            "Edge Detection 1",
            "Edge Detection 2",
            "Edge Detection 3",
            "Sharpen",
            "Box Blur",
            "Gaussian Blur",
            "5x5 Unsharp",
    };

    private static final Kernel[] KERNELS = new Kernel[8];
    static {
        // identity
        KERNELS[0] = new Kernel(new float[] {0, 1, 0}, new float[] {0, 1, 0});
        // edge detection 1
        KERNELS[1] = new Kernel(new float[] {1, 0, -1}, new float[] {1, 0, -1});
        // edge detection 2
        KERNELS[2] = new Kernel(3);
        KERNELS[2].setValue(0, 1, 1);
        KERNELS[2].setValue(1, 0, 1);
        KERNELS[2].setValue(1, 1, -4);
        KERNELS[2].setValue(1, 2, 1);
        KERNELS[2].setValue(2, 1, 1);
        // edge detection 3
        KERNELS[3] = new Kernel(3);
        KERNELS[3].setAll(-1);
        KERNELS[3].setValue(1, 1, 8);
        // Sharpen
        KERNELS[4] = new Kernel(3);
        KERNELS[4].setValue(0, 1, -1);
        KERNELS[4].setValue(1, 0, -1);
        KERNELS[4].setValue(1, 1, 5);
        KERNELS[4].setValue(1, 2, -1);
        KERNELS[4].setValue(2, 1, -1);
        // Box Blur
        KERNELS[5] = new Kernel(3);
        KERNELS[5].setAll(1);
        KERNELS[5].setConstFactor(1 / 9f);

        // Gaussian Blur
        KERNELS[6] = new Kernel(new float[] {1, 2, 1}, new float[] {1, 2, 1});
        KERNELS[6].setConstFactor(1 / 16f);
        // 5x5 Unsharp"
        KERNELS[7] = new Kernel(new float[] {1, 4, 6, 4, 1}, new float[] {1, 4, 6, 4, 1});
        KERNELS[7].setValue(2, 2, -476);
        KERNELS[7].setConstFactor(-1 / 256f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles2_image_processing);

        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        mEffectSelectorSpinner = (Spinner) findViewById(R.id.effectSelector);
        mSelectPictureButton = (Button) findViewById(R.id.selectPictureButton);

        setupGLSurfaceView();
        setupUIWidgets();
    }

    private void setupGLSurfaceView() {
        mGLSurfaceView.setEGLContextClientVersion(2);
        // Just in case... explicitly specify using alpha bit.
        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // explicitly specify the buffer size.
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 8, 8);
        mGLSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);
        mRenderer = new MyRenderer();
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void setupUIWidgets() {
        mEffectSelectorSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item, KERNEL_EFFECT_LIST));
        mEffectSelectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.setKernel(KERNELS[position]);
                        mGLSurfaceView.requestRender();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mEffectSelectorSpinner.setSelection(0);

        mSelectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_SELECT_PICTURE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_PICTURE && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Get image failed...", Toast.LENGTH_SHORT).show();
                return;
            }
            final Uri pictureUri = data.getData();
            mGLSurfaceView.queueEvent(new Runnable() {
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
        private float[] mMvpMatrix = new float[16];

        private RectangleTexture mTexture;
        private Uri mCurrentPictureUri = null;

        private FrameBuffer mImageProcessingSource;

        private KernelPostProcessing mKernelProcessor;
        private Kernel mKernel;

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
                mGLSurfaceView.requestRender();
                bitmap.recycle();
            } catch (Exception e) {
                Toast.makeText(GLES2_Image_Processing_Activity.this,
                        "Cannot open the image file", Toast.LENGTH_SHORT).show();
            }
        }

        public void setKernel(Kernel kernel) {
            mKernel = kernel;
        }

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.android_logo);
            mTexture = new RectangleTexture(bitmap);
            bitmap.recycle();
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            if (mCurrentPictureUri != null) {
                setPictureUri(mCurrentPictureUri);
            }

            GLES20.glViewport(0, 0, width, height);


            float[] projectMatrix = new float[16];
            float[] viewMatrix = new float[16];
            float[] moduleMatrix = new float[16];

            Matrix.orthoM(projectMatrix, 0, -width / 2, width / 2, -height / 2, height / 2, 0, 2);
            Matrix.setLookAtM(viewMatrix, 0,
                    0f, 0f, 1f,
                    0f, 0f, 0f,
                    0f, 1f, 0);

            Matrix.setIdentityM(moduleMatrix, 0);
            Matrix.scaleM(moduleMatrix, 0, width, height, 1);

            Matrix.multiplyMM(mMvpMatrix, 0, projectMatrix, 0, viewMatrix, 0);
            Matrix.multiplyMM(mMvpMatrix, 0, mMvpMatrix, 0, moduleMatrix, 0);

            mImageProcessingSource = new FrameBuffer(width, height);
            mKernelProcessor = new KernelPostProcessing(GLES2_Image_Processing_Activity.this, mImageProcessingSource);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            /**
             * Render image on framebuffer.
             */
            mImageProcessingSource.bind();
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            mTexture.draw(mMvpMatrix);
            mImageProcessingSource.unbind();

            if (mKernel == null) {
                return;
            }
            /**
             * Image processing.
             */
            mKernelProcessor.draw(mMvpMatrix, mKernel);
        }
    }
}
