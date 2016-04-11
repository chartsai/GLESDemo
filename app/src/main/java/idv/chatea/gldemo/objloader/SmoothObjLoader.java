package idv.chatea.gldemo.objloader;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * This class is used to load the obj files which doesn't provide normalSum vector data.
 */
public class SmoothObjLoader extends BasicObjLoader{
    private static final String TAG = SmoothObjLoader.class.getSimpleName();

    private class Normal {
        private float[] normalSum = new float[3];
        private int counter = 0;

        public void addNormal(Float[] newNormal) {
            normalSum[0] += newNormal[0];
            normalSum[1] += newNormal[1];
            normalSum[2] += newNormal[2];
            counter ++;
        }

        public Float[] getNormal() {
            if (counter == 0) {
                return new Float[]{0f, 0f, 0f};
            } else {
                return new Float[]{normalSum[0] / counter, normalSum[1] / counter, normalSum[2] / counter};
            }
        }
    }

    @Override
    public ObjData loadObjFile(Context context, String fileName) {
        Resources r = context.getResources();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(r.getAssets().open(fileName)));
            String line;
            Vector<Float[]> positions = new Vector<>();
            Vector<Integer> positionIndices = new Vector<>();
            Vector<Float[]> textureCoords = new Vector<>();
            Vector<Integer> textureCoordIndices = new Vector<>();
            Vector<Normal> normals = new Vector<>();
            int positionCounter = 0;
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
                        normals.add(new Normal());
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
                        if (vertexDataString.length >= 2) {
                            textureIndex1 = Integer.parseInt(vertexDataString[1]);
                            if (textureIndex1 > 0) {
                                textureIndex1 = textureIndex1 - 1;
                            } else { // related index
                                textureIndex1 = textureIndex1 + textureCoordCounter;
                            }
                            textureCoordIndices.add(textureIndex1);
                        }

                        vertexDataString = data[2].split("/");
                        int positionIndex2 = Integer.parseInt(vertexDataString[0]);
                        if (positionIndex2 > 0) {
                            positionIndex2 = positionIndex2 - 1;
                        } else { // related index
                            positionIndex2 = positionIndex2 + positionCounter;
                        }
                        positionIndices.add(positionIndex2);
                        int textureIndex2 = -1;
                        if (vertexDataString.length >= 2) {
                            textureIndex2 = Integer.parseInt(vertexDataString[1]);
                            if (textureIndex2 > 0) {
                                textureIndex2 = textureIndex2 - 1;
                            } else { // related index
                                textureIndex2 = textureIndex2 + textureCoordCounter;
                            }
                            textureCoordIndices.add(textureIndex2);
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
                        if (vertexDataString.length >= 2) {
                            textureIndex3 = Integer.parseInt(vertexDataString[1]);
                            if (textureIndex3 > 0) {
                                textureIndex3 = textureIndex3 - 1;
                            } else { // related index
                                textureIndex3 = textureIndex3 + textureCoordCounter;
                            }
                            textureCoordIndices.add(textureIndex3);
                        }

                        Float[] n = calculateNormal(positions.get(positionIndex1), positions.get(positionIndex2), positions.get(positionIndex3));
                        normals.get(positionIndex1).addNormal(n);
                        normals.get(positionIndex2).addNormal(n);
                        normals.get(positionIndex3).addNormal(n);

                        vertexCounter += 3;

                        if (data.length == 5) { // 4 vertices per surface (compatible to OpenGL)
                            // this is same as 1st point
                            positionIndices.add(positionIndex1);
                            if (textureIndex1 != -1) {
                                textureCoordIndices.add(textureIndex1);
                            }

                            // this is same as 3rd point
                            positionIndices.add(positionIndex3);
                            if (textureIndex3 != -1) {
                                textureCoordIndices.add(textureIndex3);
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
                            }

                            // The normal is same has previous 3 point, ignore calculation.
                            normals.get(positionIndex1).addNormal(n);
                            normals.get(positionIndex3).addNormal(n);
                            normals.get(positionIndex4).addNormal(n);

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
                int nIndex = positionIndices.get(i);
                Float[] normal = normals.get(nIndex).getNormal();
                vertexData[i * ObjData.VERTEX_DATA_SIZE + 6] = normal[0];
                vertexData[i * ObjData.VERTEX_DATA_SIZE + 7] = normal[1];
                vertexData[i * ObjData.VERTEX_DATA_SIZE + 8] = normal[2];
            }
            objData.vertexData = vertexData;
            objData.vertexNumber = vertexCounter;
            objData.hasNormal = true;
            return objData;
        } catch (IOException e) {
            Log.d(TAG, "Cannot load Obj file");
            return null;
        }
    }

    private Float[] calculateNormal(Float[] p1, Float[] p2, Float[] p3) {
        float ux = p2[0] - p1[0];
        float uy = p2[1] - p1[1];
        float uz = p2[2] - p1[2];

        float vx = p3[0] - p1[0];
        float vy = p3[1] - p1[1];
        float vz = p3[2] - p1[2];

        return new Float[]{
                uy * vz - uz * vy,
                uz * vx - ux * vz,
                ux * vy - uy * vx
        };
    }
}
