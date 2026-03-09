public class ArraySizeChecker {
    private ArraySizeListener sizeListener;
    private static final int TARGET_SIZE = 5;

    public void setSizeListener(ArraySizeListener sl) {
        this.sizeListener = sl;
    }

    public void checkSize(String[] data) {
        if (sizeListener != null) {
            sizeListener.onSizeMatched(TARGET_SIZE, data.length);
        }
    }
}