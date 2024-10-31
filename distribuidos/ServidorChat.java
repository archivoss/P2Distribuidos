import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author RPVZ
 */
public class ServidorChat {
    private DefaultListModel mensajes = new DefaultListModel();
    private static List<HiloDeCliente> listaHilos = new ArrayList<>();
    private static List<Grupo> listaGrupos = new ArrayList<>();
    private int contadorUsuarios = 1;
    private static VentanaGestion ventanaGestion; // Instancia de VentanaGestion

    public static void main(String[] args) {
        new ServidorChat();
    }

    public ServidorChat() {
        try {
            ventanaGestion = new VentanaGestion(listaGrupos); // Inicializa la ventana de gestión
            ServerSocket socketServidor = new ServerSocket(5000);
            while (true) {
                Socket cliente = socketServidor.accept();

                String nombreUsuario = "Usuario" + contadorUsuarios;
                String rol = JOptionPane.showInputDialog(null, "Ingrese el rol del usuario: ");
                Runnable nuevoCliente = new HiloDeCliente(mensajes, cliente, nombreUsuario, rol);
                Thread hilo = new Thread(nuevoCliente);
                hilo.start();
                System.out.println("Nuevo cliente conectado: " + nombreUsuario);
                listaHilos.add((HiloDeCliente) nuevoCliente);

                // Actualiza la lista de usuarios en la ventana
                ventanaGestion.actualizarUsuarios(listaHilos, listaGrupos);

                contadorUsuarios++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<HiloDeCliente> getListaHilos() {
        return listaHilos;
    }
    public static void agregarGrupo(Grupo grupo) {
        listaGrupos.add(grupo);
        if (ventanaGestion != null) {
            ventanaGestion.actualizarUsuarios(listaHilos, listaGrupos);
        }
    }

    public static List<Grupo> getListaGrupos(){
        return listaGrupos;
    }

    public static synchronized void eliminarCliente(HiloDeCliente cliente) {
        listaHilos.remove(cliente);
    }
}


