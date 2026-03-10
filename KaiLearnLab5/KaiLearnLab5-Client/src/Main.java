import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import some.Compute;

public class Main {
    public static void main(String[] args) {
        try {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            if (args.length < 2) {
                System.out.println("Usage: Main <server_host> <numbers...>");
                return;
            }

            String serverHost = args[0];
            ArrayList<Integer> list = new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                list.add(Integer.parseInt(args[i]));
            }

            Registry registry = LocateRegistry.getRegistry(serverHost);
            Compute comp = (Compute) registry.lookup("Compute");
            Calculate task = new Calculate(list);
            String result = comp.executeTask(task);

            String[] parts = result.split(":");
            long sumEven = Long.parseLong(parts[0]);
            long sumOdd = Long.parseLong(parts[1]);

            System.out.println("Сумма чётных и положительных чисел: " + sumEven);
            System.out.println("Сумма нечётных и положительных чисел: " + sumOdd);

        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }
    }
}