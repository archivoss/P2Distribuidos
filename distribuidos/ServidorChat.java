import java.awt.CardLayout;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
    
                // Crear un JPanel para la selección de rol
                JPanel panelRoles = new JPanel();
                panelRoles.setLayout(new BoxLayout(panelRoles, BoxLayout.Y_AXIS));
    
                // Menú desplegable para seleccionar el rol
                String[] roles = {"Medico", "Administrativo"};
                JComboBox<String> comboRoles = new JComboBox<>(roles);
                panelRoles.add(new JLabel("Seleccione el rol del usuario:"));
                panelRoles.add(comboRoles);
    
                // Panel para el rol administrativo
                JPanel panelAdministrativo = new JPanel();
                String[] opcionesAdministrativas = {"Admisión", "Pabellón", "Exámenes", "Auxiliar"};
                JComboBox<String> comboAdministrativo = new JComboBox<>(opcionesAdministrativas);
                comboAdministrativo.setVisible(false); // Ocultar inicialmente
                panelAdministrativo.add(new JLabel("Seleccione la opción administrativa:"));
                panelAdministrativo.add(comboAdministrativo);
    
                // Usar CardLayout para cambiar entre los paneles
                JPanel panelPrincipal = new JPanel(new CardLayout());
                panelPrincipal.add(panelRoles, "roles");
                panelPrincipal.add(panelAdministrativo, "administrativo");
    
                // Mostrar el panel en un JOptionPane
                int result = JOptionPane.showConfirmDialog(null, panelPrincipal, "Selección de Rol", JOptionPane.OK_CANCEL_OPTION);
    
                // Manejar la lógica de selección
                String rolSeleccionado = (String) comboRoles.getSelectedItem();
                if ("Administrativo".equals(rolSeleccionado)) {
                    comboAdministrativo.setVisible(true);
                    CardLayout cl = (CardLayout) panelPrincipal.getLayout();
                    cl.show(panelPrincipal, "administrativo");
                    result = JOptionPane.showConfirmDialog(null, panelPrincipal, "Selección de Rol", JOptionPane.OK_CANCEL_OPTION);
                }
    
                // Obtener el rol final basado en la selección original
                String rol = rolSeleccionado; // Mantener el rol seleccionado en el primer combo
                if (result == JOptionPane.OK_OPTION && "Administrativo".equals(rolSeleccionado)) {
                    rol = rolSeleccionado + " - " + (String) comboAdministrativo.getSelectedItem(); // Agregar la opción administrativa
                }
    
                // Crear el nuevo cliente
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


