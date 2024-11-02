import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class VentanaGestion extends JFrame {
    private DefaultListModel<String> modeloUsuarios;
    private DefaultListModel<String> modeloGrupos;
    private JList<String> listaUsuarios;
    private JList<String> listaGrupos;
    private JButton btnAgregarUsuario;
    private List<Grupo> listaDeGrupos;

    public VentanaGestion(List<Grupo> grupos) {
        setTitle("Gestión del Servidor de Chat");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        listaDeGrupos = grupos;

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

        btnAgregarUsuario.addActionListener(e -> {
            // Lógica para agregar un usuario
            new ClienteChat();
        });

        setVisible(true);
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
