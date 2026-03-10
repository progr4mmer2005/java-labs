package compute;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Удалённый интерфейс вычислителя.
 * Клиент отправляет объект Task — сервер выполняет и возвращает результат.
 */
public interface Compute extends Remote {
    <T> T executeTask(Task<T> t) throws RemoteException;
}
