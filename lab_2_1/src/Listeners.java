interface FileOutputListener {
    void onFileOutput(String fileName, String message);
}

interface ResultListener {
    void onResultObtained(Object result);
}

interface ArraySizeListener {
    void onSizeMatched(int expected, int actual);
}
