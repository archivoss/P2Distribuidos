import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
/**
 *
 * @author RPVZ
 */
public class ControlCliente implements ActionListener, Runnable{
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private PanelCliente panel;
    private Thread hilo;
    private Socket socket;
    private JFrame ventana;
    private String nombreUsuario;
    private String rol;

    public ControlCliente(Socket socket, String nombreUsuario, String rol){
        this.socket = socket;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;

        //this.panel = panel;
        try{
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());

            creaYVisualizaVentana();
            
            hilo = new Thread(this);
            hilo.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void actionPerformed(ActionEvent evento){
        try{
            String comando = evento.getActionCommand();
            String texto = panel.getTexto();

            if(rol.equals("Admin")){
                dataOutput.writeUTF("@emergencia:" + texto);
                return;
            }

            switch (comando) {
                case "Ver Usuarios":
                    dataOutput.writeUTF("/usuarios");
                    break;
                case "Crear Grupo":
                    dataOutput.writeUTF("@creargrupo:" + texto);
                    break;
                case "Mensaje Grupo":
                    dataOutput.writeUTF("@mensajegrupo:" + texto);
                    break;
                case "Agregar Miembros":
                    dataOutput.writeUTF("@agregarmiembros:" + texto);
                    break;
                case "Mensaje Administración":
                    dataOutput.writeUTF("@mensajeadministracion:" + texto);
                    break;
                case "Todos":
                    dataOutput.writeUTF("@todos:" + texto);
                    break;
                default:
                    if (texto.equals("@desconectar")) {
                        dataOutput.writeUTF("/desconectar");
                    } else {
                        dataOutput.writeUTF(texto);
                    }
                    break;
            }
        } catch (Exception excepcion){
            excepcion.printStackTrace();
        }
    }
    @Override
    public void run(){
        try{
            while (true){
                String texto = dataInput.readUTF();
                
                if(texto.startsWith("/usuarios")){
                    String[] usuarios = texto.substring(10).split(",");
                    for(String usuario : usuarios){
                        panel.addTexto(usuario);
                    }
                    continue;
                } 
                if(texto.startsWith("/desconectar")){
                    panel.addTexto("Desconectado del servidor.");
                    panel.addTexto("\n");
                    cerrarSesion();
                    break;
                }
                else{
                    panel.addTexto(texto);
                    panel.addTexto("\n");
                }
                
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void creaYVisualizaVentana() {
        if(rol.equals("Admin")){
            ventana = new JFrame("Chat del administrador");
            ventana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            panel = new PanelCliente(ventana.getContentPane(), rol);
            panel.addActionListener(this);
            ventana.setSize(600, 400);
            ventana.setVisible(true);
        } else {
            ventana = new JFrame("Chat de " + nombreUsuario);
            ventana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            panel = new PanelCliente(ventana.getContentPane(), rol);
            panel.addActionListener(this);
            ventana.setSize(600, 400);
            ventana.setVisible(true);
        }
    }
    public void cerrarSesion() {
        try {
            dataOutput.writeUTF("SALIR");  // Notifica al servidor
            socket.close();  // Cierra el socket
            hilo.interrupt();  // Detiene el hilo
            ventana.dispose();  // Cierra la ventana
            System.out.println("Sesión cerrada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
