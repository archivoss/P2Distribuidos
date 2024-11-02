import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author RPVZ
 */
public class ControlCliente implements ActionListener, Runnable {
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private PanelCliente panel;
    private Thread hilo;
    private Socket socket;
    private JFrame ventana;
    private Timer timer;  // Timer para la ejecución recurrente

    public ControlCliente(Socket socket) {
        this.socket = socket;
        try {
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());

            creaYVisualizaVentana();
            
            hilo = new Thread(this);
            hilo.start();
            
            iniciarTimer();  // Inicia el timer para la ejecución cada 2 segundos

            /*System.out.println("OBTENGO TODO EN CONTROL CLIENTE1");
            List<HiloDeCliente> hilos = ServidorChat.getListaHilos();
            System.out.println(hilos);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iniciarTimer() {
        timer = new Timer(2000, new ActionListener() {  // 2000 ms = 2 segundos
            @Override
            public void actionPerformed(ActionEvent e) {
                generarInfoUser();
            }
        });
        timer.start();
    }

    public void generarInfoUser() {
        this.panel.cambiartextAreaUsuarios();
        // Aquí puedes agregar lógica adicional si es necesario
        /*try {
            dataOutput.writeUTF("to"); // Comunicación con el servidor
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }


    @Override
    public void actionPerformed(ActionEvent evento) {
        try {
            String comando = evento.getActionCommand();
            String texto = panel.getTexto();

            if (texto.equals("@mensajegrupo")) {
                dataOutput.writeUTF("/mensajegrupo");
                return;
            }
            if (texto.equals("@agregarmiembros")) {
                dataOutput.writeUTF("/agregarmiembros");
                return;
            }
            if (texto.equals("@creargrupo")) {
                dataOutput.writeUTF("/creargrupo");
                return;
            }
            if (texto.equals("@desconectar")) {
                dataOutput.writeUTF("/desconectar");
                return;
            } else {
                dataOutput.writeUTF(texto);
            }
            if (comando.equals("Ver Usuarios")) {
                dataOutput.writeUTF("/usuarios");
            }
        } catch (Exception excepcion) {
            excepcion.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String texto = dataInput.readUTF();
                
                if (texto.startsWith("/usuarios")) {
                    String[] usuarios = texto.substring(10).split(",");
                    for (String usuario : usuarios) {
                        panel.addTexto(usuario);
                    }
                    continue;
                } 
                if (texto.startsWith("/desconectar")) {
                    panel.addTexto("Desconectado del servidor.");
                    panel.addTexto("\n");
                    cerrarSesion();
                    break;
                } else {
                    panel.addTexto(texto);
                    panel.addTexto("\n");
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void creaYVisualizaVentana() {
        ventana = new JFrame("Cliente Chat");
        ventana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel = new PanelCliente(ventana.getContentPane());
        panel.addActionListener(this);
        ventana.setSize(600, 400);
        ventana.setVisible(true);
    }

    public void cerrarSesion() {
        try {
            dataOutput.writeUTF("SALIR");  // Notifica al servidor
            socket.close();  // Cierra el socket
            hilo.interrupt();  // Detiene el hilo
            timer.stop();  // Detiene el timer
            ventana.dispose();  // Cierra la ventana
            System.out.println("Sesión cerrada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}