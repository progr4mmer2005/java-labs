package engine;



import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import compute.Task;
import compute.Compute;


public class ComputeEngine implements Compute {

    public ComputeEngine() {
        super();
    }


    @Override
    public <T> T executeTask(Task<T> t) {
        System.out.println("[СЕРВЕР] Получена задача: " + t.getClass().getName());
        T result = t.execute();
        System.out.println("[СЕРВЕР] Задача выполнена, результат отправлен клиенту.");
        return result;
    }

    public static void main(String[] args) {

        // Установка менеджера безопасности (обязательно для RMI)
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            String name = "Compute";

            // Создаём экземпляр вычислителя
            Compute engine = new ComputeEngine();

            // Экспортируем удалённый объект (порт 0 = выбирается автоматически)
            Compute stub = (Compute) UnicastRemoteObject.exportObject(engine, 0);

            // Получаем реестр RMI (стандартный порт 1099)
            Registry registry = LocateRegistry.getRegistry();

            // Регистрируем объект в реестре под именем "Compute"
            registry.rebind(name, stub);

            System.out.println("[СЕРВЕР] ComputeEngine зарегистрирован в реестре RMI.");
            System.out.println("[СЕРВЕР] Ожидание задач от клиентов...");

        } catch (Exception e) {
            System.err.println("[СЕРВЕР] Ошибка при запуске ComputeEngine:");
            e.printStackTrace();
        }
    }
}
