import java.io.*;

public class Logger {
    private PrintWriter writer;
    private String path;
    private FileOutputListener listener;
    private boolean isLogging = false;

    public Logger(String path) throws IOException {
        this.path = path;
        this.writer = new PrintWriter(new FileWriter(path, false));
    }

    public void setListener(FileOutputListener listener) {
        this.listener = listener;
    }

    public void log(String message) {
        if (isLogging) {
            writeToOutputs(message);
            return;
        }
        try {
            isLogging = true;
            if (listener != null) listener.onFileOutput(path, message);
            writeToOutputs(message);
        } finally {
            isLogging = false;
        }
    }

    private void writeToOutputs(String message) {
        System.out.println(message);
        writer.println(message);
        writer.flush();
    }

    public void close() { if (writer != null) writer.close(); }
}