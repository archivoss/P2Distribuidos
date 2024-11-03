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
    private static List<HiloDeCliente> listaUsuariosTotal = new ArrayList<>();
    private int contadorUsuarios = 1;
    private static VentanaGestion ventanaGestion; // Instancia de VentanaGestion

    public static void main(String[] args) {
        new ServidorChat();
    }

    public ServidorChat() {
        try {

            

            // Leer el archivo JSON y crear los grupos
            leerGruposDesdeJson("grupos.json");
            leerUsuariosDesdeJson("usuarios.json");
            ventanaGestion = new VentanaGestion(listaGrupos, listaUsuariosTotal);
            ventanaGestion.actualizarUsuarios(listaHilos, listaGrupos);
            ServerSocket socketServidor = new ServerSocket(5000);
            while (true) {
                Socket cliente = socketServidor.accept();
                String[] datos = ventanaGestion.getDatos();

                System.out.println("Usuario conectado: " + datos[0]);
                //validar si el usuario existe
            
                for (HiloDeCliente usuario : listaUsuariosTotal) {
                    if (usuario.getNombreUsuario().equals(datos[0])) {
                        if (usuario.getContrasena().equals(datos[1])) {
                            Runnable nuevoCliente = new HiloDeCliente(mensajes, cliente, datos[0], usuario.getRol(), usuario.getContrasena());
                            Thread hilo = new Thread(nuevoCliente);
                            hilo.start();
                            listaHilos.add((HiloDeCliente) nuevoCliente);
                            ventanaGestion.actualizarUsuarios(listaHilos, listaGrupos);
                            break;
                        } else {
                            System.out.println("Contraseña incorrecta");
                            cliente.close();
                            continue;
                        }
                    }
                }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void leerUsuariosDesdeJson(String archivo) {
        try (FileReader reader = new FileReader(archivo)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray usuariosArray = jsonObject.getAsJsonArray("usuarios");

            for (JsonElement usuarioElement : usuariosArray) {
                JsonObject usuarioObject = usuarioElement.getAsJsonObject();
                String nombreUsuario = usuarioObject.get("nombreUsuario").getAsString();
                String contrasena = usuarioObject.get("contrasena").getAsString();
                String rol = usuarioObject.get("rol").getAsString();
                HiloDeCliente cliente = new HiloDeCliente(mensajes, null, nombreUsuario, rol, contrasena);
                listaUsuariosTotal.add(cliente);
                agregarUsuarioAGrupoPorRol(cliente, rol);
            }
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
