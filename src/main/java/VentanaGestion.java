import javax.swing.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VentanaGestion extends JFrame {
    private DefaultListModel<String> modeloUsuarios;
    private DefaultListModel<String> modeloGrupos;
    private JList<String> listaUsuarios;
    private JList<String> listaGrupos;
    private JButton btnAgregarUsuario;
    private JButton btnChatAdmin;
    private JButton btnAgregarNuevoUsuarioAJson;
    private JButton btnReiniciarContrasena;
    private List<Grupo> listaDeGrupos;
    private List<HiloDeCliente> listaDeUsuariosContecados;
    private List<HiloDeCliente> listaUsuariosTotal;
    private String nombreUsuario;
    private String contrasena;
    boolean cambioClave = false;


    public VentanaGestion(List<Grupo> grupos, List<HiloDeCliente> usuariosTotal, List<HiloDeCliente> usuariosConectados) {
        setTitle("Gestión del Servidor de Chat");
        setSize(600, 400); // Aumentar el tamaño de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        listaDeGrupos = grupos;
        listaUsuariosTotal = usuariosTotal;
        listaDeUsuariosContecados = usuariosConectados;

        // Modelo y lista para mostrar los usuarios conectados
        modeloUsuarios = new DefaultListModel<>();
        modeloGrupos = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloUsuarios);
        listaGrupos = new JList<>(modeloGrupos);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        JScrollPane scrollGrupos = new JScrollPane(listaGrupos);

        // Botones
        btnAgregarUsuario = new JButton("Conectar Usuario");
        btnChatAdmin = new JButton("Chat Admin");
        btnAgregarNuevoUsuarioAJson = new JButton("Agregar Nuevo Usuario a la BD");
        btnReiniciarContrasena = new JButton("Reiniciar Contraseña");

        // Panel para los botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayout(4, 1, 10, 10)); // 4 filas, 1 columna, 10px de espacio entre componentes
        panelBotones.add(btnAgregarUsuario);
        panelBotones.add(btnChatAdmin);
        panelBotones.add(btnAgregarNuevoUsuarioAJson);
        panelBotones.add(btnReiniciarContrasena);

        // Añadir componentes a la ventana
        add(scrollUsuarios, BorderLayout.CENTER);
        add(scrollGrupos, BorderLayout.EAST);
        add(panelBotones, BorderLayout.WEST);

        // Evento al seleccionar un grupo
        listaGrupos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String grupoSeleccionado = listaGrupos.getSelectedValue();
                resaltarMiembros(grupoSeleccionado);
            }
        });

        // Evento al hacer doble clic en un usuario
        listaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {  // Doble clic
                    String usuarioSeleccionado = listaUsuarios.getSelectedValue();
                    if (usuarioSeleccionado != null) {
                        mostrarVentanaNueva(usuarioSeleccionado);
                    }
                }
            }
        });

        btnReiniciarContrasena.addActionListener(e ->{
            reiniciarContrasena();
            JOptionPane.showMessageDialog(this, "Se ha reiniciado la contraseña del usuario.");
        });

        btnAgregarUsuario.addActionListener(e -> {
            // Lógica para agregar un usuario
            nombreUsuario = JOptionPane.showInputDialog("Ingrese el nombre del usuario");

            if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del usuario no puede estar vacío.");
                return;
            }

            for (HiloDeCliente usuario : listaUsuariosTotal) {
                if (usuario.getNombreUsuario().equals(nombreUsuario)) {
                    leerUsuariosDesdeJson("usuarios.json");
                    if(!cambioClave){
                        String nuevaContrasena = JOptionPane.showInputDialog("¿Primera vez? !Actualicemos tu contraseña " + nombreUsuario + "!");
                        if (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()) {
                            usuario.setContrasena(nuevaContrasena);
                            actualizarContrasenaEnJson(nombreUsuario, nuevaContrasena, true);
                            JOptionPane.showMessageDialog(this, "Contraseña actualizada exitosamente.");
                            break;
                        } else {
                            JOptionPane.showMessageDialog(this, "La nueva contraseña no puede estar vacía.");
                        }
                    }
                    
                }
            }


            contrasena = JOptionPane.showInputDialog("Ingrese la contraseña del usuario");

            boolean usuarioExiste = false;

            for (HiloDeCliente usuario : listaDeUsuariosContecados) {
                if (usuario.getNombreUsuario().equals(nombreUsuario)) {
                    usuarioExiste = true;
                    break;
                }
            }
            if(usuarioExiste){
                JOptionPane.showMessageDialog(this, "El usuario ya está conectado");
                return;
            }else{
                for(HiloDeCliente usuario : listaUsuariosTotal){
                    if(usuario.getNombreUsuario().equals(nombreUsuario) && usuario.getContrasena().equals(contrasena)){
                        getDatos();
                        new ClienteChat(usuario.getNombreUsuario(), usuario.getRol());
                        return;
                    }
                }
            }
            
        });

        btnChatAdmin.addActionListener(e -> {
            // Lógica para abrir el chat de administración
            new ClienteChat("Admin", "Admin");
        });

        btnAgregarNuevoUsuarioAJson.addActionListener(e -> {
            // Lógica para agregar un nuevo usuario a la base de datos
            String nombreUsuario = JOptionPane.showInputDialog("Ingrese el nombre del usuario");
            String rut = JOptionPane.showInputDialog("Ingrese el RUT del usuario");
            String correo = JOptionPane.showInputDialog("Ingrese el correo del usuario");
            String contrasena = JOptionPane.showInputDialog("Ingrese la contraseña del usuario");
            String rol = JOptionPane.showInputDialog("Ingrese el rol del usuario");

            if (nombreUsuario != null && rut != null && correo != null && contrasena != null) {
                agregarUsuarioAJson(nombreUsuario, rut, correo, contrasena, rol);
                actualizarListaUsuarios();
            }
        });

        setVisible(true);
    }

    private void reiniciarContrasena() {
        String nombreUsuario = JOptionPane.showInputDialog("Ingrese el nombre del usuario para reiniciar la contraseña");

        if (nombreUsuario != null && !nombreUsuario.trim().isEmpty()) {
            boolean usuarioEncontrado = false;

            for (HiloDeCliente usuario : listaUsuariosTotal) {
                if (usuario.getNombreUsuario().equals(nombreUsuario)) {
                    usuarioEncontrado = true;
                    actualizarContrasenaEnJson(usuario.getNombreUsuario(),"" ,false);
                }
            }

            if (!usuarioEncontrado) {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "El nombre del usuario no puede estar vacío.");
        }
    }

    // Método para actualizar la contraseña en el archivo JSON
    private void actualizarContrasenaEnJson(String nombreUsuario, String contrasena,boolean cambioClave) {
        try (FileReader reader = new FileReader("usuarios.json")) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray usuariosArray = jsonObject.getAsJsonArray("usuarios");

            for (JsonElement usuarioElement : usuariosArray) {
                JsonObject usuarioObject = usuarioElement.getAsJsonObject();
                if (usuarioObject.get("nombreUsuario").getAsString().equals(nombreUsuario)) {
                    usuarioObject.addProperty("contrasena", contrasena);
                    usuarioObject.addProperty("cambioClave", cambioClave);
                    break;
                }
            }

            try (FileWriter writer = new FileWriter("usuarios.json")) {
                gson.toJson(jsonObject, writer);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al guardar la nueva contraseña en la base de datos.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al leer la base de datos.");
        }
    }

    private void agregarUsuarioAJson(String nombreUsuario, String rut, String correo, String contrasena, String rol) {
        try (FileReader reader = new FileReader("usuarios.json")) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray usuariosArray = jsonObject.getAsJsonArray("usuarios");

            JsonObject nuevoUsuario = new JsonObject();
            nuevoUsuario.addProperty("nombreUsuario", nombreUsuario);
            nuevoUsuario.addProperty("rol", rol); // Asignar rol según sea necesario
            nuevoUsuario.addProperty("contrasena", " ");
            nuevoUsuario.addProperty("rut", rut);
            nuevoUsuario.addProperty("correo", correo);
            nuevoUsuario.addProperty("mensajes", " ");
            nuevoUsuario.addProperty("cambioClave", false);

            usuariosArray.add(nuevoUsuario);

            try (FileWriter writer = new FileWriter("usuarios.json")) {
                gson.toJson(jsonObject, writer);
                JOptionPane.showMessageDialog(this, "Usuario agregado exitosamente a la base de datos.");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al guardar el usuario en la base de datos.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al leer la base de datos.");
        }
    }

    public String[] getDatos(){
        String [] datos = {nombreUsuario, contrasena};
        return datos;
    }

    // Método para actualizar las listas
    public void actualizarUsuarios(List<HiloDeCliente> usuarios, List<Grupo> grupos) {
        modeloUsuarios.clear();
        modeloGrupos.clear();
        listaDeGrupos = grupos;

        for (HiloDeCliente usuario : usuarios) {
            modeloUsuarios.addElement(usuario.getNombreUsuario() + " (" + usuario.getRol() + ")");
        }
        for (Grupo grupo : grupos) {
            modeloGrupos.addElement(grupo.getNombreGrupo());
        }
    }

    // Método para actualizar la lista de usuarios
    private void actualizarListaUsuarios() {
        listaUsuariosTotal.clear();
        try (FileReader reader = new FileReader("usuarios.json")) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray usuariosArray = jsonObject.getAsJsonArray("usuarios");

            for (JsonElement usuarioElement : usuariosArray) {
                JsonObject usuarioObject = usuarioElement.getAsJsonObject();
                String nombreUsuario = usuarioObject.get("nombreUsuario").getAsString();
                String contrasena = usuarioObject.get("contrasena").getAsString();
                String rol = usuarioObject.get("rol").getAsString();
                HiloDeCliente cliente = new HiloDeCliente(null, null, nombreUsuario, rol, contrasena);
                listaUsuariosTotal.add(cliente);
            }
            actualizarUsuarios(listaDeUsuariosContecados, listaDeGrupos);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al leer la base de datos.");
        }
    }

    // Método para resaltar los miembros del grupo seleccionado
    private void resaltarMiembros(String grupoSeleccionado) {
        listaUsuarios.clearSelection();
        for (Grupo grupo : listaDeGrupos) {
            if (grupo.getNombreGrupo().equals(grupoSeleccionado)) {
                List<Integer> indicesSeleccionados = new ArrayList<>();
                for (int i = 0; i < modeloUsuarios.getSize(); i++) {
                    String nombreUsuario = modeloUsuarios.getElementAt(i);
                    for (HiloDeCliente usuario : grupo.getListaMiembros()) {
                        if (usuario.getNombreUsuario().equals(nombreUsuario)) {
                            indicesSeleccionados.add(i);
                        }
                    }
                }
                int[] indicesArray = indicesSeleccionados.stream().mapToInt(i -> i).toArray();
                listaUsuarios.setSelectedIndices(indicesArray);
                return;
            }
        }
    }

    // Método para mostrar una nueva ventana con el nombre del usuario seleccionado
    private void mostrarVentanaNueva(String usuarioSeleccionado) {
        Long tiempoConectado = (long) 1000;
        List<HiloDeCliente> usuarios = ServidorChat.getListaHilos();
        Map<String, Integer> conteoMensajes = new HashMap<>(); // Para almacenar el conteo de mensajes
        
        for (HiloDeCliente usuario : usuarios) {
            if (usuarioSeleccionado.contains(usuario.nombreUsuario)) {
                tiempoConectado = usuario.tiempoConectado().toSeconds();
                ArrayList<String> listaMensajes = usuario.getlistaMensajes();

                conteoMensajes = contarMensajes(listaMensajes); // Obtener el conteo de mensajes
            }
        }
        
        // Crear la ventana nueva
        JDialog nuevaVentana = new JDialog(this, "Detalles del Usuario", true);
        nuevaVentana.setSize(300, 200); // Ajustar el tamaño si es necesario
        nuevaVentana.setLayout(new FlowLayout());
        
        JLabel labelNombreUsuario = new JLabel("Usuario: " + usuarioSeleccionado);
        JLabel tiempo = new JLabel("Tiempo Online: " + tiempoConectado + " segundos");
        nuevaVentana.add(labelNombreUsuario);
        nuevaVentana.add(tiempo);
        
        // Crear un JTextArea para mostrar el conteo de mensajes
        JTextArea textoMensajes = new JTextArea(5, 20); // Puedes ajustar el tamaño
        textoMensajes.setEditable(false); // Hacerlo no editable
        
        // Construir el texto para el JTextArea
        StringBuilder mensajesTexto = new StringBuilder("Destinatarios de Mensajes:\n");
        for (Map.Entry<String, Integer> entry : conteoMensajes.entrySet()) {
            mensajesTexto.append(entry.getKey()).append(": ").append(entry.getValue()).append(" mensajes").append("\n");
        }
        
        // Establecer el texto en el JTextArea
        textoMensajes.setText(mensajesTexto.toString());
        nuevaVentana.add(new JScrollPane(textoMensajes)); // Agregar el JTextArea a la ventana
        
        nuevaVentana.setLocationRelativeTo(this);
        nuevaVentana.setVisible(true);
        
    }

    public static Map<String, Integer> contarMensajes(ArrayList<String> listaMensajes) {
        Map<String, Integer> conteo = new HashMap<>();

        // Recorrer cada mensaje en la lista
        for (String mensaje : listaMensajes) {
            // Si el mensaje ya está en el mapa, incrementar el contador
            if (conteo.containsKey(mensaje)) {
                conteo.put(mensaje, conteo.get(mensaje) + 1);
            } else {
                // Si no está, agregarlo con un conteo inicial de 1
                conteo.put(mensaje, 1);
            }
        }

        return conteo;
    }

    public void leerUsuariosDesdeJson(String archivo) {
        try (FileReader reader = new FileReader(archivo)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray usuariosArray = jsonObject.getAsJsonArray("usuarios");
    
            for (JsonElement usuarioElement : usuariosArray) {
                JsonObject usuarioObject = usuarioElement.getAsJsonObject();
                String nombreUsuarioJson = usuarioObject.get("nombreUsuario").getAsString();
    
                if (nombreUsuarioJson.equals(nombreUsuario)) {
                    System.out.println("usuario: " + nombreUsuarioJson + " " + usuarioObject.get("cambioClave").getAsBoolean());
                    cambioClave = usuarioObject.get("cambioClave").getAsBoolean();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al leer la base de datos.");
        }
    }
}
