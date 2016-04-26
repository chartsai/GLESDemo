package idv.chatea.gldemo.postprocess;

/**
 * Kernel is an matrix that used in filter effect and used for convolution operation.
 * Due to performance reason, the suggestion kernel size is 3x3, and don't larger than 5x5
 *
 * Check https://en.wikipedia.org/wiki/Kernel_(image_processing) for more information.
 */
public class Kernel {

    /**
     * This value is same as the value in GLSL.
     */
    public static final int MAX_KERNEL_SIZE = 16;

    /**
     * The constant factor of kernel matrix. Default value is 1.
     * This is convenience for multiple/divide all value in kernel matrix.
     */
    private float mConstFactor = 1f;
    private float[] mKernelMatrix;
    private int mLength;

    public Kernel(int edgeLength) {
        if (edgeLength > MAX_KERNEL_SIZE) {
            throw new IllegalArgumentException("The Length is larger than maximum kernel size");
        }
        mLength = edgeLength;
        mKernelMatrix = new float[edgeLength * edgeLength];
    }

    public Kernel(float[] leftColumnVector, float[] rightRowVector) {
        if (leftColumnVector.length != rightRowVector.length) {
            throw new IllegalArgumentException("Lengths or column vector and row vector are different");
        }
        int length = leftColumnVector.length;
        if (length > MAX_KERNEL_SIZE) {
            throw new IllegalArgumentException("The Length is larger than maximum kernel size");
        }
        mLength = length;
        mKernelMatrix = new float[mLength * mLength];
        setRowMultipleColumn(leftColumnVector, rightRowVector);
    }

    public float getConstFactor() {
        return mConstFactor;
    }

    public int getKernelLength() {
        return mLength;
    }

    public float[] getFlattenKernelMatrix() {
        float[] returnedKernelMatrix = new float[mLength * mLength];
        for (int i = 0; i < mKernelMatrix.length; i++) {
            returnedKernelMatrix[i] = mKernelMatrix[i] * mConstFactor;
        }
        return returnedKernelMatrix;
    }

    /**
     * Used to set the const factor of kernel matrix.
     * Default value is 1f
     * @param factor the const factor of kernel matrix.
     */
    public void setConstFactor(float factor) {
        mConstFactor = factor;
    }

    public void setValue(int row, int column, float value) {
        if (row >= mLength && column >= mLength) {
            throw new IndexOutOfBoundsException("Out of kernel matrix bounds");
        }
        // column based matrix
        mKernelMatrix[row + column * mLength] = value;
    }

    public void setRowMultipleColumn(float[] leftColumnVector, float[] rightRowVector) {
        if (leftColumnVector.length != mLength || rightRowVector.length != mLength) {
            throw new IllegalArgumentException("row or column vector doesn't match the length of kernel atrix");
        }
        for (int row = 0; row < mLength; row++) {
            for (int column = 0; column < mLength; column++) {
                mKernelMatrix[row + column * mLength] = leftColumnVector[row] * rightRowVector[column];
            }
        }
    }

    /**
     * Set all slot as the specify value
     * @param value the value for all
     */
    public void setAll(float value) {
        for (int i = 0; i < mKernelMatrix.length; i++) {
            mKernelMatrix[i] = value;
        }
    }
}
