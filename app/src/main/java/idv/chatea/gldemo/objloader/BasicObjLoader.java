package idv.chatea.gldemo.objloader;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * TODO load texture.
 */
public class BasicObjLoader {
    private static final String TAG = BasicObjLoader.class.getSimpleName();

    public static class ObjData {
        public static final int POSITION_SIZE = 3;
        public static final int TEXTURE_COORD_SIZE = 3;
        public static final int NORMAL_SIZE = 3;
        public static final int VERTEX_DATA_SIZE = POSITION_SIZE + TEXTURE_COORD_SIZE + NORMAL_SIZE;

        public static final int POSITION_OFFSET = 0;
        public static final int TEXTURE_COORD_OFFSET = POSITION_SIZE;
        public static final int NORMAL_OFFSET = POSITION_SIZE + TEXTURE_COORD_SIZE;

        public float[] vertexData;
        public int vertexNumber;

        public boolean hasNormal;
    }

    public ObjData loadObjFile(Context context, String fileName) {
        Resources r = context.getResources();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(r.getAssets().open(fileName)));
            String line;
            Vector<Float[]> positions = new Vector<>();
            Vector<Integer> positionIndices = new Vector<>();
            Vector<Float[]> textureCoords = new Vector<>();
            Vector<Integer> textureCoordIndices = new Vector<>();
            Vector<Float[]> normals = new Vector<>();
            Vector<Integer> normalIndices = new Vector<>();
            int positionCounter = 0;
            int normalCounter = 0;
            int textureCoordCounter = 0;
            int vertexCounter = 0;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("[ ]+");
                switch (data[0].trim()) {
                    case "v":
                        positions.add(new Float[]{
                                Float.parseFloat(data[1]),
                                Float.parseFloat(data[2]),
                                Float.parseFloat(data[3])
                        });
                        positionCounter++;
                        break;
                    case "vt":
                        if (data.length == 3) {
                            textureCoords.add(new Float[]{
                                    Float.parseFloat(data[1]),
                                    Float.parseFloat(data[2]),
                                    0f
                            });
                        } else if (data.length == 4) {
                            textureCoords.add(new Float[]{
                                    Float.parseFloat(data[1]),
                                    Float.parseFloat(data[2]),
                                    Float.parseFloat(data[3]),
                            });
                        }
                        textureCoordCounter++;
                        break;
                    case "vn":
                        normals.add(new Float[]{
                                Float.parseFloat(data[1]),
                                Float.parseFloat(data[2]),
                                Float.parseFloat(data[3])
                        });
                        normalCounter++;
                        break;
                    case "f":
                        String[] vertexDataString;
                        vertexDataString = data[1].split("/");
                        int positionIndex1 = Integer.parseInt(vertexDataString[0]);
                        if (positionIndex1 > 0) {
                            positionIndex1 = positionIndex1 - 1;
                        } else { // related index
                            positionIndex1 = positionIndex1 + positionCounter;
                        }
                        positionIndices.add(positionIndex1);
                        int textureIndex1 = -1;
                        int normalIndex1 = -1;
                        if (vertexDataString.length >= 2) {
                            textureIndex1 = Integer.parseInt(vertexDataString[1]);
                            if (textureIndex1 > 0) {
                                textureIndex1 = textureIndex1 - 1;
                            } else { // related index
                                textureIndex1 = textureIndex1 + textureCoordCounter;
                            }
                            textureCoordIndices.add(textureIndex1);
                            if (vertexDataString.length >= 3) {
                                normalIndex1 = Integer.parseInt(vertexDataString[2]);
                                if (normalIndex1 > 0) {
                                    normalIndex1 = normalIndex1 - 1;
                                } else { // related index
                                    normalIndex1 = normalIndex1 + normalCounter;
                                }
                                normalIndices.add(normalIndex1);
                            }
                        }

                        vertexDataString = data[2].split("/");
                        int positionIndex2 = Integer.parseInt(vertexDataString[0]);
                        if (positionIndex2 > 0) {
                            positionIndex2 = positionIndex2 - 1;
                        } else { // related index
                            positionIndex2 = positionIndex2 + positionCounter;
                        }
                        positionIndices.add(positionIndex2);
                        if (vertexDataString.length >= 2) {
                            int textureIndex2 = Integer.parseInt(vertexDataString[1]);
                            if (textureIndex2 > 0) {
                                textureIndex2 = textureIndex2 - 1;
                            } else { // related index
                                textureIndex2 = textureIndex2 + textureCoordCounter;
                            }
                            textureCoordIndices.add(textureIndex2);
                            if (vertexDataString.length >= 3) {
                                int normalIndex2 = Integer.parseInt(vertexDataString[2]);
                                if (normalIndex2 > 0) {
                                    normalIndex2 = normalIndex2 - 1;
                                } else { // related index
                                    normalIndex2 = normalIndex2 + normalCounter;
                                }
                                normalIndices.add(normalIndex2);
                            }
                        }

                        vertexDataString = data[3].split("/");
                        int positionIndex3 = Integer.parseInt(vertexDataString[0]);
                        if (positionIndex3 > 0) {
                            positionIndex3 = positionIndex3 - 1;
                        } else { // related index
                            positionIndex3 = positionIndex3 + positionCounter;
                        }
                        positionIndices.add(positionIndex3);
                        int textureIndex3 = -1;
                        int normalIndex3 = -1;
                        if (vertexDataString.length >= 2) {
                            textureIndex3 = Integer.parseInt(vertexDataString[1]);
                            if (textureIndex3 > 0) {
                                textureIndex3 = textureIndex3 - 1;
                            } else { // related index
                                textureIndex3 = textureIndex3 + textureCoordCounter;
                            }
                            textureCoordIndices.add(textureIndex3);
                            if (vertexDataString.length >= 3) {
                                normalIndex3 = Integer.parseInt(vertexDataString[2]);
                                if (normalIndex3 > 0) {
                                    normalIndex3 = normalIndex3 - 1;
                                } else { // related index
                                    normalIndex3 = normalIndex3 + normalCounter;
                                }
                                normalIndices.add(normalIndex3);
                            }
                        }

                        vertexCounter += 3;

                        if (data.length == 5) { // 4 vertices per surface (compatible to OpenGL)
                            // The 1st point is same as point 1
                            positionIndices.add(positionIndex1);
                            if (textureIndex1 != -1) {
                                textureCoordIndices.add(textureIndex1);
                                if (normalIndex1 != -1) {
                                    normalIndices.add(normalIndex1);
                                }
                            }

                            // The 2nd point is same as point 3
                            positionIndices.add(positionIndex3);
                            if (textureIndex3 != -1) {
                                textureCoordIndices.add(textureIndex3);
                                if (normalIndex3 != -1) {
                                    normalIndices.add(normalIndex3);
                                }
                            }

                            vertexDataString = data[4].split("/");
                            int positionIndex4 = Integer.parseInt(vertexDataString[0]);
                            if (positionIndex4 > 0) {
                                positionIndex4 = positionIndex4 - 1;
                            } else { // related index
                                positionIndex4 = positionIndex4 + positionCounter;
                            }
                            positionIndices.add(positionIndex4);
                            if (vertexDataString.length >= 2) {
                                int textureIndex4 = Integer.parseInt(vertexDataString[1]);
                                if (textureIndex4 > 0) {
                                    textureIndex4 = textureIndex4 - 1;
                                } else { // related index
                                    textureIndex4 = textureIndex4 + textureCoordCounter;
                                }
                                textureCoordIndices.add(textureIndex4);
                                if (vertexDataString.length >= 3) {
                                    int normalIndex4 = Integer.parseInt(vertexDataString[2]);
                                    if (normalIndex4 > 0) {
                                        normalIndex4 = normalIndex4 - 1;
                                    } else { // related index
                                        normalIndex4 = normalIndex4 + normalCounter;
                                    }
                                    normalIndices.add(normalIndex4);
                                }
                            }

                            vertexCounter += 3;
                        }
                        break;
                    default:
                        // Others, do nothing so far.
                        break;
                }
            }

            ObjData objData = new ObjData();

            int singleVertexSize = 3;
            if (textureCoordCounter > 0) {
                singleVertexSize += 3;
                if (normalCounter > 0) {
                    singleVertexSize += 3;
                }
            }

            float[] vertexData = new float[vertexCounter * ObjData.VERTEX_DATA_SIZE];
            for (int i = 0; i < vertexCounter; i++) {
                int pIndex = positionIndices.get(i);
                vertexData[i * ObjData.VERTEX_DATA_SIZE + 0] = positions.get(pIndex)[0];
                vertexData[i * ObjData.VERTEX_DATA_SIZE + 1] = positions.get(pIndex)[1];
                vertexData[i * ObjData.VERTEX_DATA_SIZE + 2] = positions.get(pIndex)[2];
                if (singleVertexSize >= 6) {
                    int tIndex = textureCoordIndices.get(i);
                    vertexData[i * ObjData.VERTEX_DATA_SIZE + 3] = textureCoords.get(tIndex)[0];
                    vertexData[i * ObjData.VERTEX_DATA_SIZE + 4] = textureCoords.get(tIndex)[1];
                    vertexData[i * ObjData.VERTEX_DATA_SIZE + 5] = textureCoords.get(tIndex)[2];
                }
                if (singleVertexSize >= 9) {
                    int nIndex = normalIndices.get(i);
                    vertexData[i * ObjData.VERTEX_DATA_SIZE + 6] = normals.get(nIndex)[0];
                    vertexData[i * ObjData.VERTEX_DATA_SIZE + 7] = normals.get(nIndex)[1];
                    vertexData[i * ObjData.VERTEX_DATA_SIZE + 8] = normals.get(nIndex)[2];
                }
            }
            objData.vertexData = vertexData;
            objData.vertexNumber = vertexCounter;
            objData.hasNormal = normalCounter > 0;
            return objData;
        } catch (IOException e) {
            Log.d(TAG, "Cannot load Obj file");
            return null;
        }
    }

    public static void manualCalculateNormal(BasicObjLoader.ObjData data) {
        /** TODO for GLES 3.1, use compute shader. */
        // U = p2 - p1
        // V = p3 - p1
        //
        // Nx = UyVz - UzVy
        // Ny = UzVx - UxVz
        // Nz = UxVy - UyVx
        float[] vd = data.vertexData;
        final int ts = ObjData.VERTEX_DATA_SIZE * 3; // triangle stride
        for (int i = 0; i < data.vertexNumber / 3; i++) {
            /**
             * float[] p1 = {vd[i * ts + 0], vd[i * ts + 1], vd[i * ts + 2]};
             * float[] p2 = {vd[i * ts + 9], vd[i * ts + 10], vd[i * s + 11]};
             * float[] p3 = {vd[i * ts + 18], vd[i * ts + 19], vd[i * s + 20]};
             */
            float ux = vd[i * ts + 9] - vd[i * ts + 0];
            float uy = vd[i * ts + 10] - vd[i * ts + 1];
            float uz = vd[i * ts + 11] - vd[i * ts + 2];

            float vx = vd[i * ts + 18] - vd[i * ts + 0];
            float vy = vd[i * ts + 19] - vd[i * ts + 1];
            float vz = vd[i * ts + 20] - vd[i * ts + 2];

            float[] n = {
                    uy * vz - uz * vy,
                    uz * vx - ux * vz,
                    ux * vy - uy * vx
            };

            vd[i * ts + 6] = n[0];
            vd[i * ts + 7] = n[1];
            vd[i * ts + 8] = n[2];
            vd[i * ts + 15] = n[0];
            vd[i * ts + 16] = n[1];
            vd[i * ts + 17] = n[2];
            vd[i * ts + 24] = n[0];
            vd[i * ts + 25] = n[1];
            vd[i * ts + 26] = n[2];
        }
    }
}
