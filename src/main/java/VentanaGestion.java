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
import java.util.ArrayList;
import java.util.List;

public class VentanaGestion extends JFrame {
    private DefaultListModel<String> modeloUsuarios;
    private DefaultListModel<String> modeloGrupos;
    private JList<String> listaUsuarios;
    private JList<String> listaGrupos;
    private JButton btnAgregarUsuario;
    private JButton btnChatAdmin;
    private JButton btnAgregarNuevoUsuarioAJson;
    private List<Grupo> listaDeGrupos;
    private List<HiloDeCliente> listaDeUsuariosContecados;
    private List<HiloDeCliente> listaUsuariosTotal;
    private String nombreUsuario;
    private String contrasena;


    public VentanaGestion(List<Grupo> grupos, List<HiloDeCliente> usuariosTotal, List<HiloDeCliente> usuariosConectados) {
        setTitle("Gestión del Servidor de Chat");
        setSize(400, 300);
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

        // Botón para agregar usuarios
        btnAgregarUsuario = new JButton("Agregar Usuario");
        btnChatAdmin = new JButton("Chat Admin");
        btnAgregarNuevoUsuarioAJson = new JButton("Agregar Nuevo Usuario a la BD");


        // Añadir el botón y el scroll a la ventana
        add(scrollUsuarios, BorderLayout.CENTER);
        add(scrollGrupos, BorderLayout.EAST);
        add(btnChatAdmin, BorderLayout.WEST);
        add(btnAgregarUsuario, BorderLayout.SOUTH);
        add(btnAgregarNuevoUsuarioAJson, BorderLayout.NORTH);


        // Evento al seleccionar un grupo
        listaGrupos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String grupoSeleccionado = listaGrupos.getSelectedValue();
                resaltarMiembros(grupoSeleccionado);
            }
        });

        btnAgregarUsuario.addActionListener(e -> {
            // Lógica para agregar un usuario
            nombreUsuario = JOptionPane.showInputDialog("Ingrese el nombre del usuario");
            contrasena = JOptionPane.showInputDialog("Ingrese la contraseña del usuario");

            for(HiloDeCliente usuario : listaUsuariosTotal){
                System.out.println(usuario.getNombreUsuario());
                if(usuario.getNombreUsuario().equals(nombreUsuario) && usuario.getContrasena().equals(contrasena)){
                    System.out.println("Usuario ya existe");
                    getDatos();
                    new ClienteChat(usuario.getNombreUsuario(), usuario.getRol());
                    return;
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

    private void agregarUsuarioAJson(String nombreUsuario, String rut, String correo, String contrasena, String rol) {
        try (FileReader reader = new FileReader("usuarios.json")) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonArray usuariosArray = jsonObject.getAsJsonArray("usuarios");

            JsonObject nuevoUsuario = new JsonObject();
            nuevoUsuario.addProperty("nombreUsuario", nombreUsuario);
            nuevoUsuario.addProperty("rol", rol); // Asignar rol según sea necesario
            nuevoUsuario.addProperty("contrasena", contrasena);
            nuevoUsuario.addProperty("conectado", false);
            nuevoUsuario.addProperty("rut", rut);
            nuevoUsuario.addProperty("correo", correo);

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
        listaDeGrupos = grupos;  // Actualizamos la lista de grupos

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
    // Limpiamos las selecciones anteriores
    listaUsuarios.clearSelection();

    // Buscamos el grupo seleccionado
    for (Grupo grupo : listaDeGrupos) {
        if (grupo.getNombreGrupo().equals(grupoSeleccionado)) {
            // Preparamos los índices de los usuarios que deben ser seleccionados
            List<Integer> indicesSeleccionados = new ArrayList<>();
            
            // Iteramos sobre los usuarios en la lista para ver si están en el grupo
            for (int i = 0; i < modeloUsuarios.getSize(); i++) {
                String nombreUsuario = modeloUsuarios.getElementAt(i);
                for (HiloDeCliente usuario : grupo.getListaMiembros()) {
                    
                    if (usuario.getNombreUsuario().equals(nombreUsuario)) {
                        indicesSeleccionados.add(i);  // Guardamos el índice
                    }
                }
            }

            // Seleccionamos los índices correspondientes
            int[] indicesArray = indicesSeleccionados.stream().mapToInt(i -> i).toArray();
            listaUsuarios.setSelectedIndices(indicesArray);
            return;  // Salimos una vez terminado
        }
    }
}

}
