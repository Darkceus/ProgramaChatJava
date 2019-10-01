package ejemploservidort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private static final Map<String, PrintWriter> CONECTADOS = new TreeMap<>();

    public static void main(String[] args) {
        cargarDatos();
        System.out.println("El Servidor de Chat está en línea...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        } catch (IOException e) {}
    }
    
    private static void cargarDatos() {
        try {
            File archivo = new File("Usuarios.usuarios");
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
            if (archivo.length() > 0) {
                FileInputStream fis = new FileInputStream("Usuarios.usuarios");
                ObjectInputStream ois = new ObjectInputStream(fis);
                USUARIOS = (Map<String, Usuario>) ois.readObject();
                ois.close();
                fis.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Hubo un error al abrir, "+e);
        }
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
                + "Muestra tu lista de Usuarios Bloqueados,/cnombre nuevo_nombre       Cambias tu nombre,/eusuario       Eliminas tu usuario";
        private final String SALIR = "salir";
        private final String BLOQUEAR = "bloquear";
        private final String DESBLOQUEAR = "desbloquear";
        private final String MOSTRAR = "listau";
        private final String MOSTRAR_B = "listab";
        private final String CAMBIAR_NOMBRE = "cnombre";
        private final String ELIMINAR_USUARIO = "eusuario";
        private final String SEPARADOR = ",";
        private final String INFO_NOMBRE = "El nombre debe estar en Alfanumérico.,No debe estar vacío,No debe tener espacios vacíos,No debe tener más de 15 caracteres, Y no debe ser igual a cualquiera de estos:," + AYUDA + " - " + SALIR + " - " + BLOQUEAR + " - " + DESBLOQUEAR + " - " + MOSTRAR + " - " + MOSTRAR_B + " - " + CAMBIAR_NOMBRE + " - " + ELIMINAR_USUARIO;
        private String nombres = "";
        private String conectados = "";
        private String desconectados = "";
        private String bloqueados = "";
        private FileOutputStream fos;
        private ObjectOutputStream oos;
        

        public Handler(Socket socket) {
            this.socket = socket;
        }
        
        private void guardarDatos() {
            try {
                fos = new FileOutputStream("Usuarios.usuarios");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(USUARIOS);
                oos.close();
                fos.close();
            } catch (IOException e) {
                System.out.println("Hubo un error al guardar, " + e);
            }
        }
        
        private int sinRegistrar() {
            out.println("SUBMITNAME");
            nombre = in.nextLine();
            if (nombre == null || nombre.isEmpty() || nombre.equals("") || !nombre.matches(PATRON_NOMBRE) || nombre.indexOf(' ') >= 0 || nombre.equalsIgnoreCase(AYUDA) || nombre.equalsIgnoreCase(SALIR) || nombre.equalsIgnoreCase(BLOQUEAR) || nombre.equalsIgnoreCase(DESBLOQUEAR) || nombre.equalsIgnoreCase(MOSTRAR) || nombre.equalsIgnoreCase(MOSTRAR_B) || nombre.equalsIgnoreCase(CAMBIAR_NOMBRE) || nombre.equalsIgnoreCase(ELIMINAR_USUARIO) || nombre.startsWith("/") || nombre.length() > 15) {
                out.println("INFO2MESSAGE " + INFO_NOMBRE);
                return 1;
            }
            if (nombre.equals("null")) {
                prueba = true;
                return 0;
            }
            if (USUARIOS.containsKey(nombre)) {
                out.println("INFOMESSAGE El usuario " + nombre + " ya está registrado.");
                return 1;
            }
            return 2;
        }
        
        private int Registrado() {
            out.println("2SUBMITNAME");
            nombre = in.nextLine();
            if (nombre == null || nombre.isEmpty() || nombre.equals("") || !nombre.matches(PATRON_NOMBRE) || nombre.indexOf(' ') >= 0 || nombre.equalsIgnoreCase(AYUDA) || nombre.equalsIgnoreCase(SALIR) || nombre.equalsIgnoreCase(BLOQUEAR) || nombre.equalsIgnoreCase(DESBLOQUEAR) || nombre.equalsIgnoreCase(MOSTRAR) || nombre.equalsIgnoreCase(MOSTRAR_B) || nombre.equalsIgnoreCase(CAMBIAR_NOMBRE) || nombre.startsWith("/") || nombre.length() > 15) {
                out.println("INFO2MESSAGE " + INFO_NOMBRE);
                return 1;
            }
            if (nombre.equals("null")) {
                prueba = true;
                return 0;
            }
            if (!USUARIOS.containsKey(nombre)) {
                out.println("INFOMESSAGE El usuario " + nombre + " no existe.");
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
                        if(CONECTADOS.containsKey(nom)){
                            CONECTADOS.get(nombre).println("MESSAGE [MP a " + nom + "] " + nombre + ": " + sub);
                            if (!USUARIOS.get(nom).contiene(nombre)) {
                                CONECTADOS.get(nom).println("MESSAGE [MP] " + nombre + ": " + sub);
                            }
                        } else {
                            out.println("INFOMESSAGE El Usuario " + nom + " no está conectado.");
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
                            if(CONECTADOS.containsKey(nom)){
                                if (!USUARIOS.get(nombre).contiene(nom)) {
                                    if (USUARIOS.get(nombre).insertarLista(nom)) {
                                        CONECTADOS.get(nombre).println("INFOMESSAGE Has bloqueado a " + nom);
                                        guardarDatos();
                                    } else {
                                        out.println("INFOMESSAGE Hubo un error al bloquear.");
                                    }
                                } else {
                                    out.println("INFOMESSAGE Ya tienes bloqueado a " + nom + ".");
                                }
                            } else {
                                out.println("INFOMESSAGE El Usuario " + nom + " no está conectado.");
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
                            if (CONECTADOS.containsKey(nom)) {
                                if (USUARIOS.get(nombre).contiene(nom)) {
                                    if (USUARIOS.get(nombre).quitarLista(nom)) {
                                        CONECTADOS.get(nombre).println("INFOMESSAGE Has desbloqueado a " + nom);
                                        guardarDatos();
                                    } else {
                                        out.println("INFOMESSAGE Hubo un error al desbloquear.");
                                    }
                                } else {
                                    out.println("INFOMESSAGE No tienes bloqueado a " + nom + ".");
                                }
                            } else {
                                out.println("INFOMESSAGE El Usuario " + nom + " no está conectado.");
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
            CONECTADOS.values().forEach((usuarios) -> {
                usuarios.println("MESSAGE " + nombre + " "+ mensaje);
            });
        }
        
        private void mensajes(String input) {
            USUARIOS.keySet().forEach((nombre2) -> {
                if (!USUARIOS.get(nombre2).contiene(nombre) && CONECTADOS.containsKey(nombre2)) {
                    CONECTADOS.get(nombre2).println("MESSAGE " + nombre + ": " + input);
                }
            });
        }
        
        private void mostrarUsuarios() {
            nombres = "Lista de Usuarios:"+SEPARADOR;
            USUARIOS.keySet().forEach((nombre2) -> {
                if (!nombre.equals(nombre2)) {
                    if (CONECTADOS.containsKey(nombre2)) {
                        conectados += nombre2 + "  " + "Conectado" + SEPARADOR;
                    } else {
                        desconectados += nombre2 + "  " + "Desconectado" + SEPARADOR;
                    }
                }
            });
            nombres += conectados + desconectados;
            CONECTADOS.get(nombre).println("INFO3MESSAGE " + nombres);
            nombres = "";
            conectados = "";
            desconectados = "";
        }
        
        private void mostrarBloqueados() {
            bloqueados = "Lista de Usuarios Bloqueados:"+SEPARADOR;
            bloqueados += USUARIOS.get(nombre).mostrarBloqueados();
            CONECTADOS.get(nombre).println("INFO3MESSAGE " + bloqueados);
            bloqueados = "";
        }
        
        private void cambiarNombre(int espacio, String input) {
            if (espacio >= 0 && espacio < input.length()) {
                String nombre2 = input.substring(espacio + 1, input.length());
                if (nombre2 == null || nombre2.isEmpty() || nombre2.equals("") || nombre2.equals("null") || !nombre2.matches(PATRON_NOMBRE) || nombre2.indexOf(' ') >= 0 || nombre2.equalsIgnoreCase(AYUDA) || nombre2.equalsIgnoreCase(SALIR) || nombre2.equalsIgnoreCase(BLOQUEAR) || nombre2.equalsIgnoreCase(DESBLOQUEAR) || nombre2.equalsIgnoreCase(MOSTRAR) || nombre2.equalsIgnoreCase(MOSTRAR_B) || nombre2.equalsIgnoreCase(CAMBIAR_NOMBRE) || nombre2.equalsIgnoreCase(ELIMINAR_USUARIO) || nombre2.startsWith("/") || nombre2.length() > 15) {
                    out.println("INFO2MESSAGE " + INFO_NOMBRE);
                } else {
                    if (!nombre.equals(nombre2)) {
                        if (!USUARIOS.containsKey(nombre2) && !CONECTADOS.containsKey(nombre2)) {
                            synchronized (USUARIOS) {
                                Usuario usuario = USUARIOS.remove(nombre);
                                PrintWriter pw = CONECTADOS.remove(nombre);
                                USUARIOS.put(nombre2, usuario);
                                CONECTADOS.put(nombre2, pw);
                                USUARIOS.values().forEach((usuarios) -> {
                                    if (usuarios.contiene(nombre)) {
                                        usuarios.reemplazar(nombre, nombre2);
                                    }
                                });
                                guardarDatos();
                                CONECTADOS.get(nombre2).println("NAMEACCEPTED " + nombre2);
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
        
        private boolean eliminarUsuario() {
            CONECTADOS.get(nombre).println("DELETEUSER");
            String res = in.nextLine();
            if (res.equals("si")) {
                synchronized (USUARIOS) {
                    USUARIOS.remove(nombre);
                    CONECTADOS.remove(nombre);
                    USUARIOS.values().forEach((usuarios) -> {
                        if (usuarios.contiene(nombre)) {
                            usuarios.quitarLista(nombre);
                        }
                    });
                    guardarDatos();
                    mensaje(" ha eliminado su cuenta");
                    prueba = true;
                    return prueba;
                }
            }
            return false;
        }

        @Override
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    out.println("INICIO");
                    String res = in.nextLine();
                    if (res.equals("simon")) {
                        int res2 = sinRegistrar();
                        switch (res2) {
                            case 0: {
                                return;
                            }
                            case 1: {
                                continue;
                            }
                        }
                    } else {
                        int res2 = Registrado();
                        switch (res2) {
                            case 0: {
                                return;
                            }
                            case 1: {
                                continue;
                            }
                        }
                    }
                    
                    synchronized (USUARIOS) {
                        if (!USUARIOS.containsKey(nombre) && !CONECTADOS.containsKey(nombre)) {
                            USUARIOS.put(nombre, new Usuario());
                            CONECTADOS.put(nombre, out);
                            guardarDatos();
                            CONECTADOS.get(nombre).println("INFOMESSAGE Usuario "+ nombre +" creado");
                            CONECTADOS.get(nombre).println("INFOMESSAGE Bienvenido "+nombre);
                            break;
                        } else if (USUARIOS.containsKey(nombre)) {
                            CONECTADOS.put(nombre, out);
                            CONECTADOS.get(nombre).println("INFOMESSAGE Bienvenido "+nombre);
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
                        if (input.startsWith("/") && !input2.startsWith("/"+AYUDA) && !input2.startsWith("/"+SALIR) && !input2.startsWith("/"+BLOQUEAR) && !input2.startsWith("/"+DESBLOQUEAR) && !input2.startsWith("/"+MOSTRAR) && !input2.startsWith("/"+MOSTRAR_B) && !input2.startsWith("/"+CAMBIAR_NOMBRE) && !input2.startsWith("/"+ELIMINAR_USUARIO)) {
                            mensajePrivado(espacio, input);
                        } else {
                            if (input.toLowerCase().startsWith("/"+SALIR)) {
                                return;
                            } else if (input.toLowerCase().startsWith("/"+AYUDA)) {
                               CONECTADOS.get(nombre).println("INFO2MESSAGE "+AYUDA_C);
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
                            } else if (input.toLowerCase().startsWith("/"+ELIMINAR_USUARIO)) {
                                if (eliminarUsuario()) {
                                    return;
                                }
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
                        CONECTADOS.remove(nombre);
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
