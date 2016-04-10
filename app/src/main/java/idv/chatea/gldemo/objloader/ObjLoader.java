package idv.chatea.gldemo.objloader;

import android.content.Context;

public interface ObjLoader {

    public ObjData loadObjFile(Context context, String fileName);

    public static class ObjData {
        public float[] vertexData;
        public float[] textureData;
        public float[] normalData;
        public int[] indexData;
    }
}
