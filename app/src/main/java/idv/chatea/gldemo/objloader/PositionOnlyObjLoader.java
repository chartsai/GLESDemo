package idv.chatea.gldemo.objloader;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This loader only load position data.
 * Because it only load position data, it can be render by glDrawElements.
 */
public class PositionOnlyObjLoader {
    private static final String TAG = PositionOnlyObjLoader.class.getSimpleName();

    private static final int[] FIRST_TRIANGLE_ORDER = {1, 2, 3};
    private static final int[] SECOND_TRIANGLE_ORDER = {1, 3, 4};

    /**
     * This position data is for glDrawElements.
     */
    public static class ObjPositionData {
        public float[] positions;
        public int[] indices;
    }

    /**
     * The returned object has position and index data.
     * Use glDrawElements() to render it.
     * @param context
     * @param fileName The pull-path file name in assets.
     * @return
     */
    public ObjPositionData loadObjFile(Context context, String fileName) {
        Resources r = context.getResources();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(r.getAssets().open(fileName)));
            String line;
            List<Float> vertices = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();
            int vertexCounter = 0;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("[ ]+");
                switch (data[0].trim()) {
                    case "v":
                        vertices.add(Float.parseFloat(data[1]));
                        vertices.add(Float.parseFloat(data[2]));
                        vertices.add(Float.parseFloat(data[3]));
                        vertexCounter++;
                        break;
                    case "f":
                        for (int i: FIRST_TRIANGLE_ORDER) {
                            String[] vertexDataString = data[i].split("/");
                            /** vertexDataString[0] is for v */
                            int index = Integer.parseInt(vertexDataString[0]);
                            if (index > 0) {
                                index = index - 1;
                            } else { // related index
                                index = index + vertexCounter;
                            }
                            indices.add(index);
                        }
                        if (data.length == 5) { // 4 vertices per surface (compatible to OpenGL)
                            for (int i: SECOND_TRIANGLE_ORDER) {
                                String[] vertexDataString = data[i].split("/");
                                /** vertexDataString[0] is for v */
                                int index = Integer.parseInt(vertexDataString[0]);
                                if (index > 0) {
                                    index = index - 1;
                                } else { // related index
                                    index = index + vertexCounter;
                                }
                                indices.add(index);
                            }
                        }
                        break;
                    default:
                        /** position only, ignore other **/
                        break;
                }
            }
            ObjPositionData objData = new ObjPositionData();

            float[] vertexData = new float[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) {
                vertexData[i] = vertices.get(i);
            }
            objData.positions = vertexData;

            int[] indexData = new int[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                indexData[i] = indices.get(i);
            }
            objData.indices = indexData;

            return objData;
        } catch (IOException e) {
            Log.d(TAG, "Cannot load Obj file");
            return null;
        }
    }
}
