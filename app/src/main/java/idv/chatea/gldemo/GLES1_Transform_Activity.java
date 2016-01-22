package idv.chatea.gldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.chatea.gldemo.gles10.ColorTriangle;

public class GLES1_Transform_Activity extends Activity {

    private final String[] mTransformOrderList = {
            String.format("%12s -> %12s -> %12s", "Translation", "Rotation", "Scaling"),
            String.format("%12s -> %12s -> %12s", "Translation", "Scaling", "Rotation"),
            String.format("%12s -> %12s -> %12s", "Rotation", "Translation", "Scaling"),
            String.format("%12s -> %12s -> %12s", "Rotation", "Scaling", "Translation"),
            String.format("%12s -> %12s -> %12s", "Scaling", "Translation", "Rotation"),
            String.format("%12s -> %12s -> %12s", "Scaling", "Rotation", "Translation"),
    };

    private final TransformCommand[] mTransformCommands = {
            new TransformCommand(TransformType.Translation, TransformType.Rotation, TransformType.Scaling),
            new TransformCommand(TransformType.Translation, TransformType.Scaling, TransformType.Rotation),
            new TransformCommand(TransformType.Rotation, TransformType.Translation,  TransformType.Scaling),
            new TransformCommand(TransformType.Rotation, TransformType.Scaling, TransformType.Translation),
            new TransformCommand(TransformType.Scaling, TransformType.Translation, TransformType.Rotation),
            new TransformCommand(TransformType.Scaling, TransformType.Rotation, TransformType.Translation),
    };

    private TransformCommand currentCommands = mTransformCommands[0];

    private float mPosition;
    private float mAngle;
    private float mScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gles1_transform);

        GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(1);
        glSurfaceView.setRenderer(new MyRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        Spinner spinner = (Spinner) findViewById(R.id.transformOrderSpinner);
        spinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item, mTransformOrderList));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCommands = mTransformCommands[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setSelection(0);

        SeekBar translateSeekBar = (SeekBar) findViewById(R.id.translationSeekBar);
        translateSeekBar.setMax(20);
        translateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // position between -1 and 1
                mPosition = (progress - 10) / 10f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        translateSeekBar.setProgress(10);

        SeekBar rotateSeekBar = (SeekBar) findViewById(R.id.rotationSeekBar);
        rotateSeekBar.setMax(360);
        rotateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // angle is between -180 and 180
                mAngle = progress - 180;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        rotateSeekBar.setProgress(180);

        SeekBar scaleSeekBar = (SeekBar) findViewById(R.id.scaleSeekBar);
        scaleSeekBar.setMax(10);
        scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // scale factor is between 0.5 and 1.5
                mScale = 1 + ((progress - 5) / 10f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        scaleSeekBar.setProgress(5);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {

        private ColorTriangle mTriangle;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            mTriangle = new ColorTriangle();

            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            GLU.gluLookAt(gl, 0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            gl.glMatrixMode(GL10.GL_MODULATE);

            currentCommands.execute(gl);

            mTriangle.draw(gl);
        }
    }

    private enum TransformType {
        Translation,
        Rotation,
        Scaling,
    }

    private class TransformCommand {

        private TransformType[] types;

        TransformCommand(TransformType first, TransformType second, TransformType third) {
            // The first pushed effect
            types = new TransformType[] {third, second, first};
        }

        public void execute(GL10 gl) {
            for (TransformType type: types) {
                performTransform(gl, type);
            }
        }

        public void performTransform(GL10 gl, TransformType type) {
            switch (type) {
                case Translation:
                    gl.glTranslatef(mPosition, 0.0f, 0.0f);
                    break;
                case Rotation:
                    gl.glRotatef(mAngle, 0.0f, 0.0f, 1.0f);
                    break;
                case Scaling:
                    gl.glScalef(mScale, mScale, mScale);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown TransformType");
            }
        }
    }
}
