import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner console = new Scanner(System.in)) {
            System.out.print("Введите путь к файлу данных: ");
            String dataPath = console.nextLine();

            try (BufferedReader reader = new BufferedReader(new FileReader(dataPath))) {
                String journalPath = reader.readLine();
                if (journalPath == null) {
                    System.err.println("Ошибка: Файл пуст!");
                    return;
                }

                Logger logger = new Logger(journalPath);

                // Теперь, когда путь к журналу известен, записываем в него приглашение и введенный путь
                logger.logToFileOnly("Введите путь к файлу данных: " + dataPath);

                FileEventHandler fileHandler = new FileEventHandler();
                fileHandler.setLogger(logger);
                logger.setListener(fileHandler);

                List<String> dataLines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        dataLines.add(line);
                    }
                }
                String[] dataArray = dataLines.toArray(new String[0]);

                NumberProcessor processor = new NumberProcessor();
                ArraySizeChecker sizeChecker = new ArraySizeChecker();

                ResultEventHandler resultHandler = new ResultEventHandler(logger);
                SizeEventHandler sizeHandler = new SizeEventHandler(logger);

                processor.setResultListener(resultHandler);
                sizeChecker.setSizeListener(sizeHandler);

                logger.log("--- Запуск процесса ---");

                sizeChecker.checkSize(dataArray);
                List<Integer> finalResult = processor.process(dataArray);

                if (finalResult != null && !finalResult.isEmpty()) {
                    logger.log("РЕЗУЛЬТАТ ОБРАБОТКИ:");
                    for (Integer num : finalResult) {
                        logger.log(String.valueOf(num));
                    }
                } else if (finalResult != null) {
                    logger.log("Список результатов пуст (все элементы были запрещены или некорректны).");
                }

                logger.log("--- Процесс завершен ---");
                logger.close();

            } catch (IOException e) {
                System.err.println("Ошибка при работе с файлами: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
    }
}