import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;


import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author RPVZ
 */
public class HiloDeCliente implements Runnable, ListDataListener {
    private DefaultListModel mensajes;
    private Socket socket;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private String rol;
    private String contrasena;
    public String nombreUsuario;
    public LocalDateTime horaConexion;
    public ArrayList<String> listaMensajes;

    private List<String> mensajesHistorial;
    private List<String> mensajesAdmin;
    private List<String> mensajesGrupo;

    public HiloDeCliente(DefaultListModel mensajes, Socket socket, String nombreUsuario, String rol,
            String contrasena) {
        this.mensajes = mensajes;
        this.socket = socket;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.contrasena = contrasena;
        this.horaConexion = LocalDateTime.now();
        this.listaMensajes = new ArrayList<>();
        this.mensajesHistorial = new ArrayList<>();
        this.mensajesAdmin = new ArrayList<>();
        this.mensajesGrupo = new ArrayList<>();

        try {
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
            mensajes.addListDataListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String procesarTextoEnriquecido(String texto) {
        // Reemplazar patrones con etiquetas HTML para formateo de texto
        texto = texto.replaceAll("\\*(.*?)\\*", "<b>$1</b>"); // Negrita
        texto = texto.replaceAll("_(.*?)_", "<i>$1</i>"); // Itálica
        texto = texto.replaceAll("~(.*?)~", "<u>$1</u>"); // Subrayado
        texto = texto.replaceAll("\\[color:(.*?)\\](.*?)\\[/color\\]", "<span style='color:$1;'>$2</span>"); // Colores

        return texto;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String texto = dataInput.readUTF();
                System.out.println("Mensaje recibido: " + texto);

                // Validación para evitar errores si el texto está vacío
                if (texto == null || texto.isEmpty()) {
                    continue; // Si es nulo o vacío, ignoramos y seguimos esperando nuevos mensajes
                }
                // Procesar texto enriquecido antes de enviarlo
                String textoFormateado = procesarTextoEnriquecido(texto);
                System.out.println(textoFormateado);

                // MENSAJE PRIVADO
                if (textoFormateado.charAt(0) == '@') {
                    List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                    String[] partes = textoFormateado.split(":", 2);

                    String nombreDestinatario = partes[0].substring(1); // Quitamos el '@'
                    String mensaje = partes[1];
                    this.listaMensajes.add(nombreDestinatario);
                    boolean usuarioEncontrado = false;

                    // Buscamos al destinatario en la lista de hilos

                    for (HiloDeCliente hilo : lista) {
                        if (hilo.getNombreUsuario().equals(nombreDestinatario)) {
                            try {
                                dataOutput.writeUTF("[Mensaje privado enviado a (" + nombreDestinatario + ")]: " + mensaje);
                                mensajesHistorial.add("[Mensaje privado enviado a (" + nombreDestinatario + ")]: " + mensaje);
                                guardarMensajeHilo(nombreUsuario, "[Mensaje privado enviado a (" + nombreDestinatario + ")]: " + mensaje);
                                hilo.mensajePrivado(nombreUsuario, mensaje, hilo);
                                usuarioEncontrado = true;
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    /*
                     * if (!usuarioEncontrado) {
                     * dataOutput.writeUTF("El usuario " + nombreDestinatario +
                     * " no se encuentra conectado.");
                     * }
                     */
                }

                if (textoFormateado.startsWith("@mensajeadministracion:")) {
                    try {
                        // Extraer la parte después de ":"
                        String[] partes = textoFormateado.split(":", 2);
                        String mensaje = partes[1];

                        List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                        List<Grupo> listaGrupos = ServidorChat.getListaGrupos();
                        List<String> gruposAdministracion = Arrays.asList("Medicos","Admision", "Pabellon", "Examenes");
                        int contador = 0;

                        for (Grupo grupo : listaGrupos) {
                            if (gruposAdministracion.contains(grupo.getNombreGrupo())) {
                                for (HiloDeCliente miembro : grupo.getListaMiembros()) {
                                    if (grupo.getNombreGrupo().equals("Medicos")) {
                                        for (HiloDeCliente hilo : lista) {
                                            if (hilo.getNombreUsuario().equals(miembro.getNombreUsuario())) {
                                                try {
                                                    hilo.dataOutput.writeUTF("[Mensaje enviado a (ADMINISTRACIÓN)]: " + mensaje);
                                                    if(contador == 0 && hilo.getNombreUsuario().equals(nombreUsuario)){
                                                        grupo.setMensajes("[Mensaje enviado a (ADMINISTRACIÓN)]: " + mensaje);
                                                        guardarMensajeHilo(hilo.getNombreUsuario(), "[Mensaje enviado a (ADMINISTRACIÓN)]: " + mensaje);
                                                        contador++;
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                    } else {
                                        for (HiloDeCliente hilo : lista) {
                                            if (hilo.getNombreUsuario().equals(miembro.getNombreUsuario())) {
                                                try {
                                                    hilo.dataOutput
                                                            .writeUTF("[Mensaje grupo (ADMINISTRACIÓN)]: " + mensaje);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                        guardarMensajeAdministracion("[Mensaje grupo (ADMINISTRACIÓN)]: " + mensaje);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (textoFormateado.startsWith("@mensajegrupo:")) {
                    try {
                        // Extraer la parte después de ":"
                        String[] partes = textoFormateado.split(":", 2);

                        List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                        List<Grupo> listaGrupos = ServidorChat.getListaGrupos();

                        if (partes.length < 2) {
                            dataOutput.writeUTF("Error: Formato incorrecto. Usa '@mensajegrupo: nombreGrupo, mensaje'");
                            return;
                        }

                        // Separar el nombre del grupo y el mensaje
                        String[] datos = partes[1].split(",", 2);
                        String nombreGrupo = datos[0].trim(); // El primer elemento es el nombre del grupo
                        String mensaje = datos[1].trim(); // El segundo elemento es el mensaje

                        // Buscar el grupo en la lista de grupos
                        for (Grupo grupo : listaGrupos) {
                            if (grupo.getNombreGrupo().equals(nombreGrupo)) {
                                for (HiloDeCliente miembro : grupo.getListaMiembros()) {
                                    try {
                                        for (HiloDeCliente hilo : lista) {
                                            if (hilo.getNombreUsuario().equals(miembro.getNombreUsuario())) {
                                                hilo.dataOutput.writeUTF(
                                                        "[Mensaje del grupo (" + nombreGrupo + ")]: " + mensaje);
                                                grupo.setMensajes(
                                                        "[Mensaje del grupo (" + nombreGrupo + ")]: " + mensaje);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            }
                        }
                        dataOutput.writeUTF("[Mensaje enviado al grupo (" + nombreGrupo + ")]: " + mensaje);
                    } catch (IOException e) {
                        System.err.println("Error al enviar mensaje de confirmación: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error al enviar mensaje de grupo: " + e.getMessage());
                    }
                }

                if (textoFormateado.startsWith("@agregarmiembros:")) {

                    try {
                        String[] partes = textoFormateado.split(":", 2);
                        boolean existe = false;
                        Grupo grupoAux = null;

                        // Separar el nombre del grupo y los usuarios
                        String[] datos = partes[1].split(",", 2);
                        String nombreGrupo = datos[0].trim(); // El primer elemento es el nombre del grupo

                        // Verificar si hay usuarios
                        String[] usuarios = (datos.length > 1) ? datos[1].split(",") : new String[0];
                        int count = 0;

                        List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                        List<Grupo> listaGrupos = ServidorChat.getListaGrupos();

                        // Verificar si el grupo existe
                        for (Grupo grupo : listaGrupos) {
                            if (grupo.getNombreGrupo().equals(nombreGrupo)) {
                                grupoAux = grupo;
                                existe = true;
                                break;
                            }
                        }
                        if (existe == true) {
                            // Verificar si los usuarios están conectados
                            for (int i = 0; i < usuarios.length; i++) {
                                for (int j = 0; j < lista.size(); j++) {
                                    if (lista.get(j).getNombreUsuario().equals(usuarios[i])) {
                                        count++;
                                        break;
                                    }
                                    if (j == lista.size() - 1) {
                                        dataOutput
                                                .writeUTF("El usuario " + usuarios[i] + " no se encuentra conectado.");
                                        return;
                                    }
                                }
                            }
                        } else {
                            dataOutput.writeUTF("El grupo " + nombreGrupo + " no existe.");
                        }

                        if (count >= 1) {
                            for (int i = 0; i < usuarios.length; i++) {
                                for (int j = 0; j < lista.size(); j++) {
                                    if (lista.get(j).getNombreUsuario().equals(usuarios[i])) {
                                        grupoAux.agregarMiembro(lista.get(j));
                                    }
                                }
                            }
                            dataOutput.writeUTF("[Usuarios agregados con éxito.]");
                        }

                    } catch (IOException e) {
                        System.err.println("Error al enviar mensaje de confirmación: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error al crear el grupo: " + e.getMessage());
                    }

                }

                if (textoFormateado.startsWith("@creargrupo:")) {
                    try {
                        // Extraer la parte después de ":"
                        String[] partes = textoFormateado.split(":", 2);

                        List<HiloDeCliente> lista = ServidorChat.getListaHilos();

                        if (partes.length < 2) {
                            dataOutput.writeUTF(
                                    "Error: Formato incorrecto. Usa '@creargrupo: nombreGrupo, usuario1, usuario2, ...'");
                            return;
                        }

                        // Separar el nombre del grupo y los usuarios
                        String[] datos = partes[1].split(",", 2);
                        String nombreGrupo = datos[0].trim(); // El primer elemento es el nombre del grupo

                        // Verificar si hay usuarios
                        String[] usuarios = (datos.length > 1) ? datos[1].split(",") : new String[0];
                        int count = 0;

                        // Verificar si los usuarios están conectados
                        for (int i = 0; i < usuarios.length; i++) {
                            for (int j = 0; j < lista.size(); j++) {
                                if (lista.get(j).getNombreUsuario().equals(usuarios[i])) {
                                    count++;
                                    break;
                                }
                                if (j == lista.size() - 1) {
                                    dataOutput.writeUTF("[El usuario " + usuarios[i] + " no se encuentra conectado.]");
                                    return;
                                }
                            }
                        }

                        if (count == usuarios.length) {
                            Grupo grupo = new Grupo(nombreGrupo);
                            for (int i = 0; i < usuarios.length; i++) {
                                for (int j = 0; j < lista.size(); j++) {
                                    if (lista.get(j).getNombreUsuario().equals(usuarios[i])) {
                                        grupo.agregarMiembro(lista.get(j));
                                    }
                                }
                            }
                            ServidorChat.agregarGrupo(grupo);
                        }

                        dataOutput.writeUTF("[Grupo creado con éxito.]");

                    } catch (IOException e) {
                        System.err.println("Error al enviar mensaje de confirmación: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error al crear el grupo: " + e.getMessage());
                    }
                }

                if (textoFormateado.equals("/usuarios")) {
                    List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                    String usuarios = "Usuarios conectados: ";
                    for (HiloDeCliente hilo : lista) {
                        usuarios += hilo.getNombreUsuario() + ", ";
                    }
                    dataOutput.writeUTF(usuarios);
                }
                if (textoFormateado.equals("/desconectar")) {

                    dataOutput.writeUTF("Desconectado del servidor.");
                    dataOutput.writeUTF("/desconectar");
                    desconectar();
                    break;
                }

                // MENSAJE GENERAL
                if (textoFormateado.charAt(0) != '@' && textoFormateado.charAt(0) != '/') { // Si no es un mensaje
                                                                                            // privado, lo enviamos a
                                                                                            // todos los clientes

                    this.listaMensajes.add("general");
                    int aux = 0;

                    // enviamos un mensaje solamente al grupo donde pertenece el usuario
                    List<Grupo> listaGrupos = ServidorChat.getListaGrupos();
                    List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                    for (Grupo grupo : listaGrupos) {
                        for (HiloDeCliente miembro : grupo.getListaMiembros()) {
                            if(miembro.getNombreUsuario().equals(nombreUsuario) && !grupo.getNombreGrupo().equals("Auxiliar")){
                                
                                for (HiloDeCliente hilo : grupo.getListaMiembros()) {
                                    for (HiloDeCliente hiloAux : lista) {
                                        if(hilo.getNombreUsuario().equals(hiloAux.getNombreUsuario())){
                                            hiloAux.dataOutput.writeUTF("[Mensaje del grupo (" + grupo.getNombreGrupo() + ") por (" + nombreUsuario + ")]: " + texto);
                                            if(aux == 0){
                                                grupo.setMensajes("[Mensaje del grupo (" + grupo.getNombreGrupo() + ") por (" + nombreUsuario + ")]: " + texto);
                                                guardarMensajeGrupo(grupo.getNombreGrupo(), "[Mensaje del grupo (" + grupo.getNombreGrupo() + ") por (" + nombreUsuario + ")]: " + texto);
                                                aux++;
                                                
                                            }
                                        }
                                    }
                                }
                            }
                            if(miembro.getNombreUsuario().equals(nombreUsuario) && grupo.getNombreGrupo().equals("Auxiliar")){
                                int contador = 0;
                                for (HiloDeCliente hilo : lista) {
                                    hilo.dataOutput.writeUTF("[Mensaje desde el grupo (Auxiliar)]: " + texto);
                                    if(contador == 0){
                                        grupo.setMensajes("[Mensaje desde el grupo (Auxiliar)]: " + texto);
                                        guardarMensajeGrupo(grupo.getNombreGrupo(), "[Mensaje desde el grupo (Auxiliar)]: " + texto);
                                        contador++;
                                    }
                                }
                            }
                        }
                        

                    }
                }
                if (textoFormateado.startsWith("@todos:")) {
                    List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                    String mensaje = texto.substring(7);
                    int aux = 0;
                    for (HiloDeCliente hilo : lista){
                        hilo.dataOutput.writeUTF("[Mensaje para todos de (" + hilo.nombreUsuario + ")]: " + mensaje);
                        mensajesHistorial.add("[Mensaje para todos de (" + hilo.nombreUsuario + ")]: " + mensaje);
                        if(hilo.getNombreUsuario().equals(nombreUsuario)){
                            hilo.setMensajesHistorial("[Enviaste un mensaje para todos]: " + mensaje);
                            if(aux == 0){
                                guardarMensajeHilo(hilo.getNombreUsuario(), "[Enviaste un mensaje para todos]: " + mensaje);
                                aux++;
                            }
                        }
                    }
                }
                if (textoFormateado.startsWith("@emergencia:")) {
                    
                    List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                    String mensaje = textoFormateado.substring(12);

                    if(lista.size()==0){
                        dataOutput.writeUTF("No hay usuarios conectados.");
                    }else{
                        dataOutput.writeUTF("[Mensaje de emergencia enviado a todos]: " + mensaje);
                        for (HiloDeCliente hilo : lista) {
                            hilo.dataOutput.writeUTF("[MENSAJE DE EMERGENCIA]: " + mensaje);
                            guardarMensajeHilo(hilo.getNombreUsuario(), "[Recibiste un mensaje de emergencia]: " + mensaje);
                        }
                    }
                    
                }
                if (textoFormateado.startsWith("@historial:")) {
                    List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                    List<Grupo> listaGrupos = ServidorChat.getListaGrupos();

                    List<String> todosLosMensajes = new ArrayList<>();
                    boolean aux = false;
                    // Separar el mensaje y capturar el nombre del grupo
                    String nombreGrupo = textoFormateado.substring(11).trim(); // Comienza desde el índice 11 y elimina
                                                                               // espacios en blanco

                    if (nombreGrupo.equals("Todos")) {

                        for (HiloDeCliente hilo : lista) {
                            if (hilo.getNombreUsuario().equals(nombreUsuario)) {
                                leerMensajeHiloDesdeJson("usuarios.json", hilo.getNombreUsuario());
                                todosLosMensajes.addAll(listaMensajes);
                                //todosLosMensajes.addAll(hilo.getMensajesHistorial());
                                break;
                            }
                        }

                        for (HiloDeCliente hilo : lista) {
                            if (hilo.getRol().equals("Admision") || hilo.getRol().equals("Pabellon")
                                    || hilo.getRol().equals("Examenes")) {
                                leerMensajesAdminDesdeJson("mensajesAdmin.json");
                                todosLosMensajes.addAll(mensajesAdmin);
                                break;
                            }
                        }

                        // ver a que grupo pertenece el usuario
                        for (Grupo grupo : listaGrupos) {
                            for (HiloDeCliente miembro : grupo.getListaMiembros()) {
                                if (miembro.getNombreUsuario().equals(nombreUsuario)) {
                                    for (HiloDeCliente hilo : lista) {
                                        if (hilo.getNombreUsuario().equals(nombreUsuario)) {
                                            leerMensajeGrupoDesdeJson("grupos.json", grupo.getNombreGrupo());
                                            todosLosMensajes.addAll(mensajesGrupo);
                                            //todosLosMensajes.addAll(grupo.getMensajes());
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        todosLosMensajes = eliminarDuplicados(todosLosMensajes);

                        for (HiloDeCliente hilo : lista) {
                            if (hilo.getNombreUsuario().equals(nombreUsuario)) {
                                dataOutput.writeUTF("");
                                dataOutput
                                        .writeUTF("-----------------HISTORIAL DE TODOS LOS MENSAJES------------------");
                                dataOutput.writeUTF("");
                                for (String mensaje : todosLosMensajes) {
                                    dataOutput.writeUTF(mensaje);
                                }
                                dataOutput.writeUTF("");
                                dataOutput.writeUTF("------------------------------------------------------------");
                                dataOutput.writeUTF("");
                            }
                        }
                    }
                    if (nombreGrupo.equals("Administracion")) {
                        leerMensajesAdminDesdeJson("mensajesAdmin.json");
                        for (HiloDeCliente hilo : lista) {
                            if (hilo.getNombreUsuario().equals(nombreUsuario) && !hilo.getRol().equals("Auxiliar")) {
                                List<String> mensajes = hilo.mensajesAdmin;
                                dataOutput.writeUTF("");
                                dataOutput.writeUTF(
                                        "-----------------HISTORIAL DE MENSAJES ADMINISTRACIÓN------------------");
                                dataOutput.writeUTF("");
                                for (int i = 0; i < mensajes.size(); i += 2) {
                                    String mensaje = mensajes.get(i);
                                    dataOutput.writeUTF(mensaje);
                                }
                                dataOutput.writeUTF("");
                                dataOutput.writeUTF(
                                        "----------------------------------------------------------------------------------------");
                                dataOutput.writeUTF("");
                            }
                            if(hilo.getNombreUsuario().equals(nombreUsuario) && hilo.getRol().equals("Auxiliar")){
                                dataOutput.writeUTF("No tienes permiso para ver el historial de mensajes de administración.");
                                
                            }
                        }
                    } else {
                        // Revisar si el usuario pertenece al grupo
                        boolean usuarioEncontrado = false;
                        for (Grupo grupo : listaGrupos) {
                            if (grupo.getNombreGrupo().equals(nombreGrupo)) {
                                for (HiloDeCliente miembro : grupo.getListaMiembros()) {
                                    if (miembro.getNombreUsuario().equals(nombreUsuario)) {
                                        usuarioEncontrado = true;
                                        break;
                                    }
                                }
                            }
                        }
                        // Enviar el historial de ese grupo al cliente solo si pertenece al grupo

                        if (usuarioEncontrado == true) {
                            for (Grupo grupo : listaGrupos) {
                                if (grupo.getNombreGrupo().equals(nombreGrupo)) {
                                    leerMensajeGrupoDesdeJson("grupos.json", nombreGrupo);
                                    for (HiloDeCliente miembro : grupo.getListaMiembros()) {
                                        System.out.println("Miembro: " + miembro.getNombreUsuario());
                                        if (miembro.getNombreUsuario().equals(nombreUsuario)) {
                                            nombreGrupo = grupo.getNombreGrupo();
                                            if (nombreGrupo.equals("Admision")) {
                                                nombreGrupo = "ADMISIÓN";
                                            }
                                            if (nombreGrupo.equals("Pabellon")) {
                                                nombreGrupo = "PABELLÓN";
                                            }
                                            if (nombreGrupo.equals("Examenes")) {
                                                nombreGrupo = "EXÁMENES";
                                            }
                                            if(nombreGrupo.equals("Medicos")){
                                                nombreGrupo = "MÉDICOS";
                                            }

                                            eliminarDuplicados(mensajesGrupo);

                                            dataOutput.writeUTF("");
                                            dataOutput.writeUTF("-----------------HISTORIAL DE MENSAJES GRUPO "
                                                    + nombreGrupo + "------------------");
                                            dataOutput.writeUTF("");
                                            for (int i = 0; i < mensajesGrupo.size(); i ++) {
                                                String mensaje = mensajesGrupo.get(i);
                                                
                                                dataOutput.writeUTF(mensaje);
                                            }
                                            dataOutput.writeUTF("");
                                            dataOutput.writeUTF(
                                                    "----------------------------------------------------------------------------------------");
                                            dataOutput.writeUTF("");
                                        }
                                    }
                                }
                            }
                        } else {
                            dataOutput.writeUTF("No tienes permiso para ver el historial de este grupo.");
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mensajePrivado (String nombreDestinatario, String mensaje, HiloDeCliente hilo) {    
        try{
            hilo.dataOutput.writeUTF("[Mensaje privado de (" + nombreDestinatario + ")]: " + mensaje);
            hilo.setMensajesHistorial("[Mensaje privado de (" + nombreDestinatario + ")]: " + mensaje);
            guardarMensajeHilo(hilo.getNombreUsuario(), "[Mensaje privado de (" + nombreDestinatario + ")]: " + mensaje);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void guardarMensajeHilo(String destinatario, String mensaje) {
        try {
            // Leer el archivo y el JSON existente
            FileReader reader = new FileReader("usuarios.json");
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            reader.close();
    
            // Obtener el array "usuarios"
            JsonArray usuariosArray = jsonObject.getAsJsonArray("usuarios");
    
            // Buscar el usuario destinatario en el JSON
            for (JsonElement usuarioElement : usuariosArray) {
                JsonObject usuarioObject = usuarioElement.getAsJsonObject();
                String nombreUsuario = usuarioObject.get("nombreUsuario").getAsString();
    
                if (nombreUsuario.equals(destinatario)) {
                    // Obtener o crear el array "mensajes" para el destinatario
                    JsonArray mensajesArray;
                    if (usuarioObject.has("mensajes")) {
                        mensajesArray = usuarioObject.getAsJsonArray("mensajes");
                    } else {
                        mensajesArray = new JsonArray();
                        usuarioObject.add("mensajes", mensajesArray);
                    }
    
                    // Crear un nuevo objeto de mensaje y añadirlo al array de mensajes
                    JsonObject nuevoMensaje = new JsonObject();
                    nuevoMensaje.addProperty("mensaje", mensaje);
                    mensajesArray.add(nuevoMensaje);
    
                    // Guardar el JSON actualizado en el archivo
                    FileWriter writer = new FileWriter("usuarios.json");
                    gson.toJson(jsonObject, writer);
                    writer.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar el mensaje en el archivo.");
        }
    }

    private void leerMensajeHiloDesdeJson(String archivo, String destinatario) {
        // Asegúrate de que mensajes esté inicializado
        if (listaMensajes == null) {
            listaMensajes = new ArrayList<>();
        }
    
        try (FileReader reader = new FileReader(archivo)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            
            // Obtener el arreglo "usuarios"
            JsonArray usuariosArray = jsonObject.getAsJsonArray("usuarios");
            if (usuariosArray != null && usuariosArray.size() > 0) {
                // Buscar el usuario destinatario en el JSON
                for (JsonElement usuarioElement : usuariosArray) {
                    JsonObject usuarioObject = usuarioElement.getAsJsonObject();
                    String nombreUsuarioJson = usuarioObject.get("nombreUsuario").getAsString();
    
                    if (nombreUsuarioJson.equals(destinatario)) {
                        // Obtener el arreglo "mensajes" dentro del usuario
                        JsonArray mensajesArray = usuarioObject.getAsJsonArray("mensajes");
                        if (mensajesArray != null) {
                            // Agregar cada mensaje a mensajes
                            for (JsonElement mensajeElement : mensajesArray) {
                                JsonObject mensajeObject = mensajeElement.getAsJsonObject();
                                String mensaje = mensajeObject.get("mensaje").getAsString(); // Asegúrate que el campo sea "mensaje"
                                listaMensajes.add(mensaje);
                            }
                        } else {
                            System.out.println("El arreglo 'mensajes' está vacío o no existe.");
                        }
                        break;
                    }
                }
            } else {
                System.out.println("El arreglo 'usuarios' está vacío o no existe.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarMensajeGrupo(String nombreGrupo, String mensaje){
        try {
            // Leer el archivo y el JSON existente
            FileReader reader = new FileReader("grupos.json");
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            reader.close();
    
            // Obtener el array "grupos"
            JsonArray gruposArray = jsonObject.getAsJsonArray("grupos");
    
            // Buscar el grupo en el JSON
            for (JsonElement grupoElement : gruposArray) {
                JsonObject grupoObject = grupoElement.getAsJsonObject();
                String nombreGrupoJson = grupoObject.get("nombreGrupo").getAsString();
    
                if (nombreGrupoJson.equals(nombreGrupo)) {
                    // Obtener o crear el array "mensajes" para el grupo
                    JsonArray mensajesArray;
                    if (grupoObject.has("mensajes")) {
                        mensajesArray = grupoObject.getAsJsonArray("mensajes");
                    } else {
                        mensajesArray = new JsonArray();
                        grupoObject.add("mensajes", mensajesArray);
                    }
    
                    // Crear un nuevo objeto de mensaje y añadirlo al array de mensajes
                    JsonObject nuevoMensaje = new JsonObject();
                    nuevoMensaje.addProperty("mensaje", mensaje);
                    mensajesArray.add(nuevoMensaje);
    
                    // Guardar el JSON actualizado en el archivo
                    FileWriter writer = new FileWriter("grupos.json");
                    gson.toJson(jsonObject, writer);
                    writer.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar el mensaje en el archivo.");
        }
    }

    private void leerMensajeGrupoDesdeJson(String archivo, String nombreGrupo) {
        // Asegúrate de que mensajesGrupo esté inicializado
        if (mensajesHistorial == null) {
            mensajesHistorial = new ArrayList<>();
        }
    
        try (FileReader reader = new FileReader(archivo)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            
            // Obtener el arreglo "grupos"
            JsonArray gruposArray = jsonObject.getAsJsonArray("grupos");
            if (gruposArray != null && gruposArray.size() > 0) {
                // Buscar el grupo en el JSON
                for (JsonElement grupoElement : gruposArray) {
                    JsonObject grupoObject = grupoElement.getAsJsonObject();
                    String nombreGrupoJson = grupoObject.get("nombreGrupo").getAsString();
    
                    if (nombreGrupoJson.equals(nombreGrupo)) {
                        // Obtener el arreglo "mensajes" dentro del grupo
                        JsonArray mensajesArray = grupoObject.getAsJsonArray("mensajes");
                        if (mensajesArray != null) {
                            // Agregar cada mensaje a mensajesGrupo
                            for (JsonElement mensajeElement : mensajesArray) {
                                JsonObject mensajeObject = mensajeElement.getAsJsonObject();
                                String mensaje = mensajeObject.get("mensaje").getAsString(); // Asegúrate que el campo sea "mensaje"
                                mensajesGrupo.add(mensaje);
                            }
                        } else {
                            System.out.println("El arreglo 'mensajes' está vacío o no existe.");
                        }
                        break;
                    }
                }
            } else {
                System.out.println("El arreglo 'grupos' está vacío o no existe.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public String getNombreUsuario(){
        return nombreUsuario;
    }

    private void desconectar() {
        try {
            ServidorChat.eliminarCliente(this);
            socket.close();
            System.out.println(nombreUsuario + " se ha desconectado.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public ArrayList<String> getlistaMensajes() {
        return this.listaMensajes;
    }

    private void guardarMensajeAdministracion(String mensaje) {
        try {
            // Leer el archivo y el JSON existente
            FileReader reader = new FileReader("mensajesAdmin.json");
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            reader.close();

            // Verificar si el campo "mensajesAdmin" existe
            JsonArray mensajesAdminArray = jsonObject.getAsJsonArray("mensajesAdmin");
            if (mensajesAdminArray == null) {
                mensajesAdminArray = new JsonArray();
                jsonObject.add("mensajesAdmin", mensajesAdminArray);
            }

            // Acceder al objeto de mensajes dentro de "mensajesAdmin"
            JsonObject mensajesObject = mensajesAdminArray.get(0).getAsJsonObject();
            JsonArray mensajesArray = mensajesObject.getAsJsonArray("mensajes");

            if (mensajesArray == null) {
                mensajesArray = new JsonArray();
                mensajesObject.add("mensajes", mensajesArray);
            }

            // Crear un nuevo mensaje y añadirlo al arreglo
            JsonObject nuevoMensaje = new JsonObject();
            nuevoMensaje.addProperty("mensaje", mensaje);
            mensajesArray.add(nuevoMensaje);

            // Guardar el JSON actualizado en el archivo
            FileWriter writer = new FileWriter("mensajesAdmin.json");
            gson.toJson(jsonObject, writer);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar el mensaje en el archivo.");
        }
    }

    private void leerMensajesAdminDesdeJson(String archivo) {
        // Asegúrate de que mensajesAdmin esté inicializado
        if (mensajesAdmin == null) {
            mensajesAdmin = new ArrayList<>();
        }

        try (FileReader reader = new FileReader(archivo)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            // Obtener el arreglo "mensajesAdmin"
            JsonArray mensajesAdminArray = jsonObject.getAsJsonArray("mensajesAdmin");
            if (mensajesAdminArray != null && mensajesAdminArray.size() > 0) {
                JsonObject mensajesObject = mensajesAdminArray.get(0).getAsJsonObject();

                // Obtener el arreglo "mensajes" dentro de "mensajesAdmin"
                JsonArray mensajesArray = mensajesObject.getAsJsonArray("mensajes");
                if (mensajesArray != null) {
                    // Agregar cada mensaje a mensajesAdmin
                    for (JsonElement mensajeElement : mensajesArray) {
                        JsonObject mensajeObject = mensajeElement.getAsJsonObject();
                        String mensaje = mensajeObject.get("mensaje").getAsString(); // Asegúrate que el campo sea
                                                                                     // "mensaje"
                        mensajesAdmin.add(mensaje);
                    }
                } else {
                    System.out.println("El arreglo 'mensajes' está vacío o no existe.");
                }
            } else {
                System.out.println("El arreglo 'mensajesAdmin' está vacío o no existe.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private List<String> eliminarDuplicados(List<String> mensajes) {
    // Filtramos los mensajes que comiencen con "["
        List<String> mensajesFiltrados = mensajes.stream()
                                                .filter(mensaje -> mensaje.startsWith("["))
                                                .collect(Collectors.toList());
        
        // Eliminamos duplicados usando LinkedHashSet y devolvemos una nueva lista
        return new ArrayList<>(new LinkedHashSet<>(mensajesFiltrados));
    }



    @Override
    public void intervalAdded(ListDataEvent e) {
        String texto = (String) mensajes.getElementAt(e.getIndex0());
        String textoFormateado = this.procesarTextoEnriquecido(texto);
        try {
            dataOutput.writeUTF(textoFormateado);
        } catch (Exception excepcion) {
            excepcion.printStackTrace();
        }
    }

    // Método para calcular el tiempo conectado
    public Duration tiempoConectado() {
        return Duration.between(horaConexion, LocalDateTime.now());
    }

    public List<String> getMensajesHistorial() {
        return mensajesHistorial;
    }

    public void setMensajesHistorial(String mensaje) {
        mensajesHistorial.add(mensaje);
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        // throw new UnsupportedOperationException("Not supported yet."); // Generated
        // from
        // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        // throw new UnsupportedOperationException("Not supported yet."); // Generated
        // from
        // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
