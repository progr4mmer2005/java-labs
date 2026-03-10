# Лабораторная работа №5 — RMI. Вариант 56

## Параметры варианта

| Пункт | Значение | Описание |
|-------|----------|----------|
| П.6   | 1        | Создавать JAR-файл пакета `compute` |
| П.7   | 1        | compute.jar для **клиента** — в **сетевой папке** |
| П.8   | 1        | compute.jar для **сервера** — в **сетевой папке** |
| П.9   | 0        | `client.policy` — **локально** на клиенте |
| П.10  | 1        | `server.policy` — в **сетевой папке** |
| П.11  | 0        | `SortTask.class` — **локально** на сервере |

## Задача (из ЛР №1)
Сортировка массива целых чисел.

---

## Структура файлов

```
── На компьютере СЕРВЕРА (C:\Work\home56\) ──────────────────────
  server\
    bin\
      engine\
        ComputeEngine.class       ← байт-код сервера
      client\
        SortTask.class            ← П.11=0: Task ЛОКАЛЬНО на сервере

── На компьютере КЛИЕНТА (C:\Work\home56\) ──────────────────────
  client\
    bin\
      client\
        ComputeClient.class       ← байт-код клиента
        SortTask.class            ← нужен для компиляции
    client.policy                 ← П.9=0: политика ЛОКАЛЬНО на клиенте

── В СЕТЕВОЙ папке (P:\Share\home56\) ───────────────────────────
  interface\
    bin\
      compute\
        Compute.class
        Task.class
    compute.jar                   ← П.6=1: JAR интерфейса
                                    П.7=1 и П.8=1: используется
                                    и клиентом и сервером отсюда
  server.policy                   ← П.10=1: политика сервера в сети
```

---

## Шаг 1. Компиляция

### 1.1 Компиляция интерфейса (compute)
```cmd
cd C:\Work\home56
javac -d interface\bin interface\src\compute\Compute.java interface\src\compute\Task.java
```

### 1.2 Создание compute.jar (П.6=1)
```cmd
cd C:\Work\home56\interface\bin
jar cvf compute.jar compute\*.class
```

### 1.3 Копируем compute.jar в сетевую папку (П.7=1, П.8=1)
```cmd
copy C:\Work\home56\interface\bin\compute.jar P:\Share\home56\compute.jar
```

### 1.4 Компиляция сервера
```cmd
javac -cp P:\Share\home56\compute.jar -d C:\Work\home56\server\bin C:\Work\home56\server\src\engine\ComputeEngine.java
```

### 1.5 Компиляция клиента и SortTask
```cmd
javac -cp P:\Share\home56\compute.jar -d C:\Work\home56\client\bin C:\Work\home56\client\src\client\SortTask.java C:\Work\home56\client\src\client\ComputeClient.java
```

### 1.6 Копируем SortTask.class локально на сервер (П.11=0)
```cmd
xcopy C:\Work\home56\client\bin\client\SortTask.class C:\Work\home56\server\bin\client\ /Y
```

### 1.7 Копируем server.policy в сетевую папку (П.10=1)
```cmd
copy C:\Work\home56\server\server.policy P:\Share\home56\server.policy
```

---

## Шаг 2. Запуск (порядок важен!)

### 2.1 Запуск rmiregistry
Запускаем из директории, где лежит пакет `compute`
(rmiregistry должен видеть интерфейсы compute):

```cmd
cd P:\Share\home56
rmiregistry
```
Оставляем это окно открытым.

### 2.2 Запуск сервера (в новом окне cmd на компьютере сервера)

```cmd
set CLASSPATH=C:\Work\home56\server\bin;P:\Share\home56\compute.jar

java -cp C:\Work\home56\server\bin;P:\Share\home56\compute.jar ^
     -Djava.rmi.server.codebase=file:/C:/Work/home56/server/bin/ ^
     -Djava.rmi.server.hostname=<IP_СЕРВЕРА> ^
     -Djava.security.policy=P:\Share\home56\server.policy ^
     engine.ComputeEngine
```

> Замените `<IP_СЕРВЕРА>` на реальный IP, например `192.168.1.10`

Сервер выведет:
```
[СЕРВЕР] ComputeEngine зарегистрирован в реестре RMI.
[СЕРВЕР] Ожидание задач от клиентов...
```

### 2.3 Запуск клиента (на компьютере клиента)

```cmd
set CLASSPATH=C:\Work\home56\client\bin;P:\Share\home56\compute.jar

java -cp C:\Work\home56\client\bin;P:\Share\home56\compute.jar ^
     -Djava.rmi.server.codebase=file:/C:/Work/home56/client/bin/ ^
     -Djava.security.policy=C:\Work\home56\client\client.policy ^
     client.ComputeClient <IP_СЕРВЕРА> 5 3 1 4 1 5 9 2 6
```

> П.4: адрес сервера и числа передаются через **командную строку**

---

## Ожидаемый вывод

**Клиент:**
```
[КЛИЕНТ] Подключение к серверу: 192.168.1.10
[КЛИЕНТ] Числа для сортировки: [5, 3, 1, 4, 1, 5, 9, 2, 6]
[КЛИЕНТ] Соединение с вычислителем установлено.
[КЛИЕНТ] Отправка задачи на сервер...
[КЛИЕНТ] Результат получен от сервера.
[КЛИЕНТ] Отсортированный массив (убывание):
  9
  6
  5
  5
  4
  3
  2
  1
  1
```

**Сервер:**
```
[СЕРВЕР] Получена задача: client.SortTask
[ЗАДАЧА] Получено чисел для сортировки: 9
[ЗАДАЧА] Входные данные:  [5, 3, 1, 4, 1, 5, 9, 2, 6]
[ЗАДАЧА] Результат (убывание): [9, 6, 5, 5, 4, 3, 2, 1, 1]
[СЕРВЕР] Задача выполнена, результат отправлен клиенту.
```

---

## Как работает RMI в данном варианте

```
Клиент                        rmiregistry           Сервер
  │                               │                    │
  │─── registry.lookup("Compute")─►                    │
  │◄── ссылка на заглушку (stub) ──┤                    │
  │                               │                    │
  │─── comp.executeTask(SortTask) ─────────────────────►│
  │    (SortTask сериализуется                          │ SortTask.execute()
  │     и передаётся на сервер)                         │ выполняется здесь
  │                                                     │
  │◄── List<Integer> (результат) ───────────────────────│
  │    (результат сериализуется                         │
  │     и возвращается клиенту)                         │
```

**Ключевые моменты варианта 56:**
- `compute.jar` лежит в **сетевой папке** — и сервер и клиент берут его оттуда
- `SortTask.class` лежит **локально на сервере** — сервер не скачивает его по сети
- `client.policy` **локально на клиенте**, `server.policy` **в сетевой папке**
- Параметры клиента (IP сервера + числа) — через **командную строку**
