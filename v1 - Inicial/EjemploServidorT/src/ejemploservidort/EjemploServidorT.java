package ejemploservidort;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EjemploServidorT {

    private static final Set<String> NOMBRES = new HashSet<>();
    private static final Set<PrintWriter> WRITERS = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("El Servidor de Chat está en línea...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        } catch (IOException e) {}
    }

    private static class Handler implements Runnable {

        private String nombre;
        private final Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    out.println("SUBMITNAME");
                    nombre = in.nextLine();
                    if (nombre == null) {
                        return;
                    }
                    synchronized (NOMBRES) {
                        if (!NOMBRES.contains(nombre)) {
                            NOMBRES.add(nombre);
                            break;
                        }
                    }
                }
                out.println("NAMEACCEPTED: " + nombre);
                for (PrintWriter writer : WRITERS) {
                    writer.println("MESSAGE " + nombre + " ha entrado");
                }
                WRITERS.add(out);
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/")) {
                        String nom = input.substring(1, ' ');
                        String sub = input.substring(' ', input.length() - 1);
                        if(NOMBRES.contains(nom)){
                            ArrayList<String> alN = new ArrayList<>(NOMBRES);
                            ArrayList<PrintWriter> alW = new ArrayList<>(WRITERS);
                            alW.get(alN.indexOf(nom)).println("MESSAGE " + nombre + ": " + sub);
                        }else{
                            
                        } 
                    }else{
                        
                    }
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    
                    for (PrintWriter writer : WRITERS) {
                        writer.println("MESSAGE " + nombre + ": " + input);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    WRITERS.remove(out);
                }
                if (nombre != null) {
                    System.out.println(nombre + " está saliendo");
                    NOMBRES.remove(nombre);
                    for (PrintWriter writer : WRITERS) {
                        writer.println("MESSAGE " + nombre + " ha salido");
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }
}