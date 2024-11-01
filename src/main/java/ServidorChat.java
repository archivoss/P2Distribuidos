import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

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

            ventanaGestion = new VentanaGestion(listaGrupos);
            // Leer el archivo JSON y crear los grupos
            leerGruposDesdeJson("grupos.json");

            
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

                // Agregar el usuario al grupo basado en su rol
                agregarUsuarioAGrupoPorRol((HiloDeCliente) nuevoCliente, rol);
                // Actualiza la lista de usuarios en la ventana
                ventanaGestion.actualizarUsuarios(listaHilos, listaGrupos);

                contadorUsuarios++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void leerGruposDesdeJson(String archivo) {
        try (FileReader reader = new FileReader(archivo)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray gruposArray = jsonObject.getAsJsonArray("grupos");

            for (JsonElement grupoElement : gruposArray) {
                JsonObject grupoObject = grupoElement.getAsJsonObject();
                String nombreGrupo = grupoObject.get("nombreGrupo").getAsString();
                Grupo grupo = new Grupo(nombreGrupo);
                listaGrupos.add(grupo);
            }
            ventanaGestion.actualizarUsuarios(listaHilos, listaGrupos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void agregarUsuarioAGrupoPorRol(HiloDeCliente cliente, String rol) {
        Grupo grupo = listaGrupos.stream()
                .filter(g -> g.getNombreGrupo().equals(rol))
                .findFirst()
                .orElse(null);

        if (grupo == null) {
            System.out.println("No se encontró el grupo con el rol: " + rol);
        }

        grupo.agregarMiembro(cliente);
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
