package ejemploservidort;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EjemploServidorT {

    private static Map<String, Usuario> USUARIOS = new TreeMap<>();

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
        private boolean prueba = false;
        private final String SIN_REPETICIONES = "[a-zA-Z0-9]{1}";
        private final String ALFANUMERICO = "[a-zA-Z0-9]+";
        private final String PATRON_NOMBRE = ALFANUMERICO;
        private final String AYUDA = "ayuda";
        private final String AYUDA_C = "Comandos:,/nombre_usuario mensaje       Envías mensaje privado,/salir     Sales del Servidor,"
                + "/bloquear nombre_usuario      Bloqueas al Usuario,/desbloquear "
                + "nombre_usuario       Desbloqueas al Usuario,/listau     Muestra la lista de Usuarios,/listab       "
                + "Muestra tu lista de Usuarios Bloqueados,/cnombre nuevo_nombre       Cambias tu nombre";
        private final String SALIR = "salir";
        private final String BLOQUEAR = "bloquear";
        private final String DESBLOQUEAR = "desbloquear";
        private final String MOSTRAR = "listau";
        private final String MOSTRAR_B = "listab";
        private final String CAMBIAR_NOMBRE = "cnombre";
        private final String SEPARADOR = ",";
        private final String INFO_NOMBRE = "El nombre debe estar en Alfanumérico.,No debe estar vacío,No debe tener espacios vacíos,No debe tener más de 15 caracteres, Y no debe ser igual a cualquiera de estos:," + AYUDA + " - " + SALIR + " - " + BLOQUEAR + " - " + DESBLOQUEAR + " - " + MOSTRAR + " - " + MOSTRAR_B + " - " + CAMBIAR_NOMBRE;
        private String nombres = "";
        private String bloqueados = "";
        

        public Handler(Socket socket) {
            this.socket = socket;
        }
        
        private int entrar() {
            out.println("SUBMITNAME");
            nombre = in.nextLine();
            if (nombre == null || nombre.isEmpty() || nombre.equals("") || !nombre.matches(PATRON_NOMBRE) || nombre.indexOf(' ') >= 0 || nombre.equalsIgnoreCase(AYUDA) || nombre.equalsIgnoreCase(SALIR) || nombre.equalsIgnoreCase(BLOQUEAR) || nombre.equalsIgnoreCase(DESBLOQUEAR) || nombre.equalsIgnoreCase(MOSTRAR) || nombre.equalsIgnoreCase(MOSTRAR_B) || nombre.equalsIgnoreCase(CAMBIAR_NOMBRE) || nombre.startsWith("/") || nombre.length() > 15) {
                out.println("INFO2MESSAGE " + INFO_NOMBRE);
                return 1;
            }
            if (nombre.equals("null")) {
                prueba = true;
                return 0;
            }
            if (USUARIOS.containsKey(nombre)) {
                out.println("INFOMESSAGE El usuario " + nombre + " ya existe.");
                return 1;
            }
            return 2;
        }
        
        private void mensajePrivado(int espacio, String input) {
            if (espacio >= 0 && espacio < input.length()) {
                String nom = input.substring(1, espacio);
                String sub = input.substring(espacio + 1, input.length());
                if (USUARIOS.containsKey(nom)) {
                    if (!nombre.equals(nom)) {
                        USUARIOS.get(nombre).getPw().println("MESSAGE [MP a " + nom + "] " + nombre + ": " + sub);
                        if (!USUARIOS.get(nom).contiene(nombre)) {
                            USUARIOS.get(nom).getPw().println("MESSAGE [MP] " + nombre + ": " + sub);
                        }
                    } else {
                        out.println("INFOMESSAGE No puedes enviarte un mensaje privado a ti mismo.");
                    }
                } else {
                    out.println("INFOMESSAGE El Usuario " + nom + " no existe.");
                }
            } else {
                out.println("INFOMESSAGE Comando no válido, digite '/ayuda' para más información.");
            }
        }
        
        private void bloquear(int espacio, String input) {
            if (espacio >= 0 && espacio < input.length()) {
                String nom = input.substring(espacio + 1, input.length());
                if (nom.indexOf(' ') < 0) {
                    if (USUARIOS.containsKey(nom)) {
                        if (!nombre.equals(nom)) {
                            if (!USUARIOS.get(nombre).contiene(nom)) {
                                if (USUARIOS.get(nombre).insertarLista(nom)) {
                                    USUARIOS.get(nombre).getPw().println("INFOMESSAGE Has bloqueado a " + nom);
                                } else {
                                    out.println("INFOMESSAGE Hubo un error al bloquear.");
                                }
                            } else {
                                out.println("INFOMESSAGE Ya tienes bloqueado a " + nom + ".");
                            }
                        } else {
                            out.println("INFOMESSAGE No puedes bloquearte a ti mismo.");
                        }
                    } else {
                        out.println("INFOMESSAGE El Usuario " + nom + " no existe.");
                    }
                } else {
                    out.println("INFOMESSAGE El nombre de usuario está mal escrito.");
                }
            } else {
                out.println("INFOMESSAGE Comando no válido, digite '/ayuda' para más información.");
            }
        }
        
        private void desbloquear(int espacio, String input) {
            if (espacio >= 0 && espacio < input.length()) {
                String nom = input.substring(espacio + 1, input.length());
                if (nom.indexOf(' ') < 0) {
                    if (USUARIOS.containsKey(nom)) {
                        if (!nombre.equals(nom)) {
                            if (USUARIOS.get(nombre).contiene(nom)) {
                                if (USUARIOS.get(nombre).quitarLista(nom)) {
                                    USUARIOS.get(nombre).getPw().println("INFOMESSAGE Has desbloqueado a " + nom);
                                } else {
                                    out.println("INFOMESSAGE Hubo un error al desbloquear.");
                                }
                            } else {
                                out.println("INFOMESSAGE No tienes bloqueado a " + nom + ".");
                            }
                        } else {
                            out.println("INFOMESSAGE No puedes desbloquearte a ti mismo.");
                        }
                    } else {
                        out.println("INFOMESSAGE El Usuario " + nom + " no existe.");
                    }
                } else {
                    out.println("INFOMESSAGE El nombre de usuario está mal escrito.");
                }
            } else {
                out.println("INFOMESSAGE Comando no válido, digite '/ayuda' para más información.");
            }
        }
        
        private void mensaje(String mensaje) {
            USUARIOS.values().forEach((usuarios) -> {
                usuarios.getPw().println("MESSAGE " + nombre + " "+ mensaje);
            });
        }
        
        private void mensajes(String input) {
            USUARIOS.keySet().forEach((nombre2) -> {
                if (!USUARIOS.get(nombre2).contiene(nombre)) {
                    USUARIOS.get(nombre2).getPw().println("MESSAGE " + nombre + ": " + input);
                }
            });
        }
        
        private void mostrarUsuarios() {
            nombres = "Lista de Usuarios:"+SEPARADOR;
            USUARIOS.keySet().forEach((nombre2) -> {
                if (!nombre.equals(nombre2)) {
                    nombres += nombre2 + SEPARADOR;
                }
            });
            USUARIOS.get(nombre).getPw().println("INFO3MESSAGE " + nombres);
            nombres = "";
        }
        
        private void mostrarBloqueados() {
            bloqueados = "Lista de Usuarios Bloqueados:"+SEPARADOR;
            for (int i = 0; i < USUARIOS.get(nombre).getTam(); i++) {
                String dato = USUARIOS.get(nombre).getDato(i);
                if (!USUARIOS.containsKey(dato)) {
                    USUARIOS.get(nombre).quitarLista(dato);
                }
            }
            bloqueados += USUARIOS.get(nombre).mostrarBloqueados();
            USUARIOS.get(nombre).getPw().println("INFO3MESSAGE " + bloqueados);
            bloqueados = "";
        }
        
        private void cambiarNombre(int espacio, String input) {
            if (espacio >= 0 && espacio < input.length()) {
                String nombre2 = input.substring(espacio + 1, input.length());
                if (nombre2 == null || nombre2.isEmpty() || nombre2.equals("") || nombre2.equals("null") || !nombre2.matches(PATRON_NOMBRE) || nombre2.indexOf(' ') >= 0 || nombre2.equalsIgnoreCase(AYUDA) || nombre2.equalsIgnoreCase(SALIR) || nombre2.equalsIgnoreCase(BLOQUEAR) || nombre2.equalsIgnoreCase(DESBLOQUEAR) || nombre2.equalsIgnoreCase(MOSTRAR) || nombre2.equalsIgnoreCase(MOSTRAR_B) || nombre2.equalsIgnoreCase(CAMBIAR_NOMBRE) || nombre2.startsWith("/") || nombre2.length() > 15) {
                    out.println("INFO2MESSAGE " + INFO_NOMBRE);
                } else {
                    if (!nombre.equals(nombre2)) {
                        if (!USUARIOS.containsKey(nombre2)) {
                            synchronized (USUARIOS) {
                                Usuario usuario = USUARIOS.remove(nombre);
                                USUARIOS.put(nombre2, usuario);
                                USUARIOS.values().forEach((usuarios) -> {
                                    if (usuarios.contiene(nombre)) {
                                        usuarios.reemplazar(nombre, nombre2);
                                    }
                                });
                                USUARIOS.get(nombre2).getPw().println("NAMEACCEPTED " + nombre2);
                                mensaje("ha cambiado su nombre a " + nombre2);
                                nombre = nombre2;
                            }
                        } else {
                            out.println("INFOMESSAGE Ya existe un Usuario con el nombre " + nombre2 + ".");
                        }
                    } else {
                        out.println("INFOMESSAGE Debes usar un nombre diferente al que tienes.");
                    }
                }
            } else {
                out.println("INFOMESSAGE Comando no válido, digite '/ayuda' para más información.");
            }
        }

        @Override
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    int res = entrar();
                    switch (res) {
                        case 0: {
                            return;
                        }
                        case 1: {
                            continue;
                        }
                    }
                    synchronized (USUARIOS) {
                        if (!USUARIOS.containsKey(nombre)) {
                            USUARIOS.put(nombre, new Usuario(out));
                            USUARIOS.get(nombre).getPw().println("INFOMESSAGE Bienvenido "+nombre);
                            break;
                        }
                    }
                }
                out.println("NAMEACCEPTED: " + nombre);
                mensaje("ha entrado");
                while (true) {
                    String input;
                    try {input = in.nextLine();} catch (Exception e) {return;}
                    if (input != null && !input.equals("") && !input.isEmpty()) {
                        int espacio = input.indexOf(' ');
                        String input2 = input.toLowerCase();
                        if (input.startsWith("/") && !input2.startsWith("/"+AYUDA) && !input2.startsWith("/"+SALIR) && !input2.startsWith("/"+BLOQUEAR) && !input2.startsWith("/"+DESBLOQUEAR) && !input2.startsWith("/"+MOSTRAR) && !input2.startsWith("/"+MOSTRAR_B) && !input2.startsWith("/"+CAMBIAR_NOMBRE)) {
                            mensajePrivado(espacio, input);
                        } else {
                            if (input.toLowerCase().startsWith("/"+SALIR)) {
                                return;
                            } else if (input.toLowerCase().startsWith("/"+AYUDA)) {
                               USUARIOS.get(nombre).getPw().println("INFO2MESSAGE "+AYUDA_C);
                            } else if (input.toLowerCase().startsWith("/"+BLOQUEAR)) {
                                bloquear(espacio, input);
                            } else if (input.toLowerCase().startsWith("/"+DESBLOQUEAR)) {
                                desbloquear(espacio, input);
                            } else if (input.toLowerCase().startsWith("/"+MOSTRAR)) {
                                mostrarUsuarios();
                            } else if (input.toLowerCase().startsWith("/"+MOSTRAR_B)) {
                                mostrarBloqueados();
                            } else if (input.toLowerCase().startsWith("/"+CAMBIAR_NOMBRE)) {
                                cambiarNombre(espacio, input);
                            } else {
                                mensajes(input);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (out != null || nombre != null) {
                    if (!prueba) {
                        USUARIOS.remove(nombre);
                        mensaje("ha salido");
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar Socket, "+e);
                }
            }
        }
    }
}
