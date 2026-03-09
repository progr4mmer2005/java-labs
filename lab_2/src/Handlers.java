class FileEventHandler implements FileOutputListener {
    private Logger logger;
    public void setLogger(Logger l) { this.logger = l; }
    @Override
    public void onFileOutput(String fileName, String message) {
        if (logger != null) logger.log("[EVENT 6] Обращение к потоку вывода в файл: " + fileName);
    }
}

class ResultEventHandler implements ResultListener {
    private Logger logger;
    public ResultEventHandler(Logger l) { this.logger = l; }

    @Override
    public void onResultObtained(Object result) {
        logger.log("[EVENT 7] Получен результат работы функции process().");
    }
}

class SizeEventHandler implements ArraySizeListener {
    private Logger logger;
    public SizeEventHandler(Logger l) { this.logger = l; }
    @Override
    public void onSizeMatched(int exp, int act) {
        if (exp == act) logger.log("[EVENT 8] В массиве число элементов равно указанному: (" + exp + ")");
    }
}