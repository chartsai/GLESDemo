package idv.chatea.gldemo.objloader;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IndexingObjLoader implements ObjLoader {

    private static final int[] FIRST_TRIANGLE_ORDER = {1, 2, 3};
    private static final int[] SECOND_TRIANGLE_ORDER = {1, 3, 4};

    /**
     * The returned object only has vertex data.
     * Use {@link android.opengl.GLES20.glDrawElements();} with index data to render.
     * @param context
     * @param fileName
     * @return
     */
    @Override
    public ObjData loadObjFile(Context context, String fileName) {
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

                            /** TODO vertexDataString[1] is for vt */
                            /** TODO vertexDataString[2] is for vn */
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

                                /** TODO vertexDataString[1] is for vt */
                                /** TODO vertexDataString[2] is for vn */
                            }
                        }
                        break;
                    case "vt":
                        // TODO implement texture
                        break;
                    case "vn":
                        // TODO implement normal
                        break;
                    default:
                        // Others, do nothing so far.
                        break;
                }
            }
            ObjData objData = new ObjData();

            float[] vertexData = new float[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) {
                vertexData[i] = vertices.get(i);
            }
            objData.vertexData = vertexData;

            int[] indexData = new int[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                indexData[i] = indices.get(i);
            }
            objData.indexData = indexData;

            return objData;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Cannot load Obj file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
