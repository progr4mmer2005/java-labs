/*
 * Вариант 56
 * Число: 609
 * Двоичный код (10 бит): 1 0 0 1 1 0 0 0 0 1
 * Перевод (0->1, 1->2):  2 1 1 2 2 1 1 1 1 2
 *
 * Столбец 1  — Число ФиО:               2 — задано в коде УО (MAX_FIGURES = 5)
 * Столбец 2  — Тип ФиО:                 1 — фигуры (круг, овал, треугольник, квадрат, прямоугольник)
 * Столбец 3  — Задание цвета:           1 — выпадающий список с названиями (6 цветов)
 * Столбец 4  — Выбор запускаемого ФиО:  2 — из текстового поля (вводится название фигуры)
 * Столбец 5  — Начальная скорость:      2 — из выпадающего списка (6 скоростей)
 * Столбец 6  — Выбор запущенного ФиО:   1 — из выпадающего списка
 * Столбец 7  — Присвоение номера:       1 — авто
 * Столбец 8  — Смена номера:            1 — нет
 * Столбец 9  — Регулировка скорости:    1 — указанием в текстовом поле
 * Столбец 10 — Изменение размера окна:  2 — да (отражение ФиО в новых границах)
 */

import java.util.*;
import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;

public class Main {
    static int count = 0;
    static final int MAX_FIGURES = 5;

    public static void main(String[] args) {
        new Figures();
    }
}

class Figures extends Frame implements Observer, ActionListener, Runnable {

    private LinkedList LL = new LinkedList();

    private Frame    controlFrame;
    private Button   startButton;
    private Button   changeSpeedButton;
    private Button   deleteButton;
    private Choice   colorChoice;
    private Choice   initSpeedChoice;
    private Choice   selectFigureChoice;
    private TextField shapeField;
    private TextField changeSpeedField;
    private Label    statusLabel;

    private Thread repaintThread;

    Figures() {
        // --- Демонстрационное окно (ДО) ---
        setTitle("Демонстрационное окно (ДО)");
        setSize(640, 520);
        setLocation(0, 0);
        setBackground(Color.white);
        setResizable(true);
        addWindowListener(new WindowAdapter2());

        // Центральный поток перерисовки ~60 fps (убирает лаги от Choice)
        repaintThread = new Thread(this, "RepaintTimer");
        repaintThread.setDaemon(true);
        repaintThread.start();

        setVisible(true);

        // Управляющее окно (УО)
        controlFrame = new Frame();
        controlFrame.setTitle("Управляющее окно (УО)  |  вариант 56");
        controlFrame.setSize(480, 430);
        controlFrame.setLocation(660, 0);
        controlFrame.setBackground(new Color(230, 230, 230));
        controlFrame.setLayout(new GridLayout(0, 2, 8, 6));
        controlFrame.addWindowListener(new WindowAdapter2());

        // Тип фигуры
        controlFrame.add(new Label("Тип фигуры:"));
        shapeField = new TextField("круг");
        controlFrame.add(shapeField);

        controlFrame.add(new Label("Подсказка:"));
        controlFrame.add(new Label("круг/овал/треугольник/квадрат/прямоугольник"));

        // Цвет
        controlFrame.add(new Label("Цвет:"));
        colorChoice = new Choice();
        colorChoice.addItem("Синий");
        colorChoice.addItem("Красный");
        colorChoice.addItem("Зелёный");
        colorChoice.addItem("Жёлтый");
        colorChoice.addItem("Чёрный");
        colorChoice.addItem("Оранжевый");
        controlFrame.add(colorChoice);

        // Начальная скорость
        controlFrame.add(new Label("Начальная скорость:"));
        initSpeedChoice = new Choice();
        initSpeedChoice.addItem("1 — Очень медленно");
        initSpeedChoice.addItem("2 — Медленно");
        initSpeedChoice.addItem("3 — Средняя");
        initSpeedChoice.addItem("4 — Быстро");
        initSpeedChoice.addItem("5 — Очень быстро");
        initSpeedChoice.addItem("6 — Максимальная");
        initSpeedChoice.select(2);
        controlFrame.add(initSpeedChoice);

        // Кнопка Пуск
        controlFrame.add(new Label(""));
        startButton = new Button("  Пуск  ");
        startButton.setActionCommand("START");
        startButton.addActionListener(this);
        controlFrame.add(startButton);

        // Разделитель
        controlFrame.add(new Label("--- Управление запущенной фигурой ---"));
        controlFrame.add(new Label(""));

        // Выбор запущенной фигуры
        controlFrame.add(new Label("Выбрать фигуру:"));
        selectFigureChoice = new Choice();
        controlFrame.add(selectFigureChoice);

        // Изменение скорости
        controlFrame.add(new Label("Новая скорость (1-6):"));
        changeSpeedField = new TextField("3");
        controlFrame.add(changeSpeedField);

        controlFrame.add(new Label(""));
        changeSpeedButton = new Button("Изменить скорость");
        changeSpeedButton.setActionCommand("SPEED");
        changeSpeedButton.addActionListener(this);
        controlFrame.add(changeSpeedButton);

        // Удаление фигуры
        controlFrame.add(new Label(""));
        deleteButton = new Button("Удалить фигуру");
        deleteButton.setBackground(new Color(200, 60, 60));
        deleteButton.setForeground(Color.white);
        deleteButton.setActionCommand("DELETE");
        deleteButton.addActionListener(this);
        controlFrame.add(deleteButton);

        // Статус
        statusLabel = new Label("Фигур запущено: 0 / " + Main.MAX_FIGURES);
        controlFrame.add(statusLabel);
        controlFrame.add(new Label(""));

        controlFrame.setVisible(true);
    }

    // Поток центральной перерисовки ДО
    public void run() {
        while (true) {
            repaint();
            try { Thread.sleep(16); } catch (InterruptedException e) { break; }
        }
    }

    // Observer: фигура уведомляет ДО об изменении координат
    public void update(Observable o, Object arg) {
        // Перерисовку делает repaintThread — здесь ничего не нужно
    }

    // Перерисовка ДО
    public void paint(Graphics g) {
        if (!LL.isEmpty()) {
            for (Object obj : LL) {
                Figure fig = (Figure) obj;
                fig.draw(g);
            }
        }
    }

    // Двойная буферизация — без мерцания
    public void update(Graphics g) {
        Image buf = createImage(getWidth(), getHeight());
        if (buf == null) return;
        Graphics bg = buf.getGraphics();
        bg.setColor(getBackground());
        bg.fillRect(0, 0, getWidth(), getHeight());
        paint(bg);
        g.drawImage(buf, 0, 0, this);
        bg.dispose();
    }

    // Обработка кнопок УО
    public void actionPerformed(ActionEvent aE) {
        String cmd = aE.getActionCommand();

        if (cmd.equals("START")) {
            if (LL.size() >= Main.MAX_FIGURES) {
                statusLabel.setText("Максимум: " + Main.MAX_FIGURES + " фигур");
                return;
            }
            String shape = shapeField.getText().trim().toLowerCase();
            if (!isValidShape(shape)) {
                statusLabel.setText("Неверный тип! круг/овал/...");
                return;
            }
            Color col   = colorFromIndex(colorChoice.getSelectedIndex());
            int   delay = delayFromLevel(initSpeedChoice.getSelectedIndex() + 1);

            Figure fig = new Figure(col, shape, delay, this);
            LL.add(fig);
            fig.addObserver(this);
            selectFigureChoice.addItem(fig.thr.getName());
            statusLabel.setText("Запущено: " + LL.size() + " / " + Main.MAX_FIGURES);

        } else if (cmd.equals("SPEED")) {
            int idx = selectFigureChoice.getSelectedIndex();
            if (idx < 0 || idx >= LL.size()) {
                statusLabel.setText("Выберите фигуру из списка");
                return;
            }
            try {
                int val = Integer.parseInt(changeSpeedField.getText().trim());
                if (val < 1 || val > 6) {
                    statusLabel.setText("Скорость: от 1 до 6");
                    return;
                }
                ((Figure) LL.get(idx)).setDelay(delayFromLevel(val));
                statusLabel.setText("Скорость изменена -> " + val);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Введите целое число 1-6");
            }

        } else if (cmd.equals("DELETE")) {
            int idx = selectFigureChoice.getSelectedIndex();
            if (idx < 0 || idx >= LL.size()) {
                statusLabel.setText("Выберите фигуру из списка");
                return;
            }
            Figure fig = (Figure) LL.get(idx);
            fig.stopFigure();
            LL.remove(idx);
            rebuildSelectChoice();
            statusLabel.setText("Удалена. Запущено: " + LL.size() + " / " + Main.MAX_FIGURES);
        }
    }

    private void rebuildSelectChoice() {
        selectFigureChoice.removeAll();
        for (Object obj : LL) {
            selectFigureChoice.addItem(((Figure) obj).thr.getName());
        }
    }

    private boolean isValidShape(String s) {
        return s.equals("круг") || s.equals("овал") || s.equals("треугольник")
                || s.equals("квадрат") || s.equals("прямоугольник");
    }

    private Color colorFromIndex(int i) {
        switch (i) {
            case 0: return Color.blue;
            case 1: return Color.red;
            case 2: return Color.green;
            case 3: return Color.yellow;
            case 4: return Color.black;
            case 5: return Color.orange;
            default: return Color.blue;
        }
    }

    private int delayFromLevel(int level) {
        switch (level) {
            case 1: return 65;
            case 2: return 48;
            case 3: return 32;
            case 4: return 20;
            case 5: return 12;
            case 6: return 6;
            default: return 32;
        }
    }
}


class Figure extends Observable implements Runnable {

    Thread  thr;
    int     x, y;
    Color   col;
    private String   shape;
    private boolean  xplus, yplus;
    private volatile int     delay;
    private volatile boolean running = true;
    private Figures  demoFrame;

    static final int W = 44;
    static final int H = 34;

    Figure(Color col, String shape, int delay, Figures demoFrame) {
        this.col       = col;
        this.shape     = shape;
        this.delay     = delay;
        this.demoFrame = demoFrame;

        xplus = true; yplus = true;

        Insets ins = demoFrame.getInsets();
        x = ins.left + 6;
        y = ins.top  + 6;

        // Случайное направление — приращения разные по осям
        Random rnd = new Random();
        int[] steps = {1, 2, 3};
        int vx = steps[rnd.nextInt(3)];
        int vy = steps[rnd.nextInt(3)];
        if (vx == vy) vy = (vy == 3) ? 1 : vy + 1;
        xStep = vx; yStep = vy;

        Main.count++;
        thr = new Thread(this, Main.count + ":" + shape + ":");
        thr.setDaemon(true);
        thr.start();
    }

    private int xStep, yStep;

    public void setDelay(int delay) { this.delay = delay; }

    public void stopFigure() {
        running = false;
        thr.interrupt();
    }

    public void run() {
        while (running) {
            Insets ins  = demoFrame.getInsets();
            int    minX = ins.left;
            int    minY = ins.top;
            int    maxX = demoFrame.getWidth()  - ins.right  - W;
            int    maxY = demoFrame.getHeight() - ins.bottom - H;
            if (maxX < minX) maxX = minX;
            if (maxY < minY) maxY = minY;

            if (x >= maxX) xplus = false;
            if (x <= minX) xplus = true;
            if (y >= maxY) yplus = false;
            if (y <= minY) yplus = true;

            if (xplus) x += xStep; else x -= xStep;
            if (yplus) y += yStep; else y -= yStep;

            setChanged();
            notifyObservers(this);

            try { Thread.sleep(delay); } catch (InterruptedException e) { break; }
        }
    }

    public void draw(Graphics g) {
        // Извлекаем номер из имени потока (формат "N:shape:")
        String num = thr.getName().split(":")[0];

        g.setColor(col);
        switch (shape) {
            case "круг":
                g.fillOval(x, y, W, W);
                break;
            case "овал":
                g.fillOval(x, y, W, H);
                break;
            case "треугольник":
                g.fillPolygon(
                        new int[]{ x + W/2, x,      x + W },
                        new int[]{ y,       y + H,  y + H },
                        3
                );
                break;
            case "квадрат":
                g.fillRect(x, y, W, W);
                break;
            case "прямоугольник":
                g.fillRect(x, y, W, H);
                break;
            default:
                g.fillOval(x, y, W, W);
                break;
        }
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(num, x + W/2 - 4, y - 4);
    }
}

// ОБРАБОТЧИК ЗАКРЫТИЯ ОКНА

class WindowAdapter2 extends WindowAdapter {
    public void windowClosing(WindowEvent wE) { System.exit(0); }
}