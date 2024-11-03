import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    private List<Grupo> listaDeGrupos;
    private List<HiloDeCliente> listaUsuariosTotal;
    private String nombreUsuario;
    private String contrasena;

    public VentanaGestion(List<Grupo> grupos, List<HiloDeCliente> usuariosTotal) {
        setTitle("Gestión del Servidor de Chat");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        listaDeGrupos = grupos;
        listaUsuariosTotal = usuariosTotal;

        // Modelo y lista para mostrar los usuarios conectados
        modeloUsuarios = new DefaultListModel<>();
        modeloGrupos = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloUsuarios);
        listaGrupos = new JList<>(modeloGrupos);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        JScrollPane scrollGrupos = new JScrollPane(listaGrupos);

        // Botón para agregar usuarios
        btnAgregarUsuario = new JButton("Agregar Usuario");

        // Añadir el botón y el scroll a la ventana
        add(scrollUsuarios, BorderLayout.CENTER);
        add(scrollGrupos, BorderLayout.EAST);
        add(btnAgregarUsuario, BorderLayout.SOUTH);

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

        setVisible(true);
    }

    public String[] getDatos() {
        return new String[]{nombreUsuario, contrasena};
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
            System.out.println("hello");
            System.out.println(usuario.nombreUsuario + "  " + usuarioSeleccionado);
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
}
