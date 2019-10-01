package ejemploclientet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class EjemploClienteT {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);

    public EjemploClienteT(String serverAddress) {
        this.serverAddress = serverAddress;
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        textField.addActionListener((ActionEvent e) -> {
            out.println(textField.getText());
            textField.setText("");
        });
    }
    
    private void getMensaje(String info) {
        JOptionPane.showMessageDialog(frame, info, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getNombre() {
        return JOptionPane.showInputDialog(frame, "Elige un nombre: ", "Selección de nombre", JOptionPane.PLAIN_MESSAGE
        );
    }
    
    private void mensaje(String line) {
        String info = line.substring(12);
        getMensaje(info);
    }
    
    private void mensaje2(String line) {
        String info = line.substring(13);
        String[] datos = info.split(",");
        String info2 = "";
        for (String dato : datos) {
            info2 += dato + "\n";
        }
        getMensaje(info2);
    }
    
    private void mensaje3(String line) {
        String info = line.substring(13);
        String[] datos = info.split(",");
        String info2 = "";
        int i = 0;
        int i2;
        boolean algo;
        if (datos.length > 1) {
            while (i < datos.length) {
                i2 = (i + 11) > datos.length ? datos.length : (i + 11);
                algo = false;
                for (; i < i2; i++) {
                    if (!algo) {
                        info2 += "Mostrados: " + (i2 - 1) + " - " + (datos.length - 1) + "\n";
                    }
                    info2 += datos[i] + "\n";
                    algo = true;
                }
                getMensaje(info2);
                info2 = "";
                i = i2;
            }
        } else {
            getMensaje("No hay usuarios en la lista");
        }
    }

    private void iniciar() {
        try {
            Socket socket = new Socket(serverAddress, 59001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if(line.startsWith("SUBMITNAME")){
                    out.println(getNombre());
                } else if (line.startsWith("INFOMESSAGE")) {
                    mensaje(line);
                } else if (line.startsWith("INFO2MESSAGE")) {
                    mensaje2(line);
                } else if (line.startsWith("INFO3MESSAGE")) {
                    mensaje3(line);
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                }
            }
        } catch(IOException e){
            System.out.println("Hubo un error al crear el Socket, "+e);
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Error en el IP");
            return;
        }
        EjemploClienteT cliente = new EjemploClienteT(args[0]);
        cliente.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cliente.frame.setVisible(true);
        cliente.iniciar();
    }
}
