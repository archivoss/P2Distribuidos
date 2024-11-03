import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;

/**
 *
 * @author RPVZ
 */
public class PanelCliente extends JPanel {
    private JScrollPane scroll;
    private JTextArea textArea;
    private JTextField textField;
    private JButton boton;
    private JButton btnLimpiarChat; // Nuevo botón para limpiar chat
    private JButton btnCrearGrupo;
    private JButton btnMensajeGrupo;
    private JButton btnAgregarMiembros;
    private JButton btnMensajeAdministracion;
    private JScrollPane scrollUsuarios;
    private JTextArea textAreaUsuarios;

    public PanelCliente(Container contenedor, String rol) {
        contenedor.setLayout(new BorderLayout());
        textArea = new JTextArea();
        scroll = new JScrollPane(textArea);

        textAreaUsuarios = new JTextArea();
        textAreaUsuarios.setEditable(false);
        scrollUsuarios = new JScrollPane(textAreaUsuarios);
        scrollUsuarios.setPreferredSize(new Dimension(105, 0));
        
        JPanel panel = new JPanel(new BorderLayout());
        textField = new JTextField(50);
        boton = new JButton("Enviar");
        btnLimpiarChat = new JButton("Limpiar Chat"); // Inicializar el botón
        btnAgregarMiembros = new JButton("Agregar Miembros");
        btnMensajeAdministracion = new JButton("Mensaje Administración");
        btnCrearGrupo = new JButton("Crear Grupo");
        btnMensajeGrupo = new JButton("Mensaje Grupo");
        
        // Configuración de colores de los botones
        boton.setBackground(new Color(173, 216, 230)); 
        Color botonesColor = new Color(255, 200, 210); // Color común para los botones

        // Asignar el mismo color a todos los botones
        boton.setBackground(new Color(173, 216, 230));
        btnLimpiarChat.setBackground(botonesColor); // Mismo color
        btnAgregarMiembros.setBackground(botonesColor);
        btnMensajeAdministracion.setBackground(botonesColor);
        btnCrearGrupo.setBackground(botonesColor);
        btnMensajeGrupo.setBackground(botonesColor);
        
        boton.setForeground(Color.black);
        btnLimpiarChat.setForeground(Color.black); // Color del texto del nuevo botón
        btnAgregarMiembros.setForeground(Color.black);
        btnMensajeAdministracion.setForeground(Color.black);
        btnCrearGrupo.setForeground(Color.black);
        btnMensajeGrupo.setForeground(Color.black);

        panel.add(textField, BorderLayout.NORTH);
        panel.add(boton, BorderLayout.CENTER);
        panel.add(btnLimpiarChat, BorderLayout.EAST); // Añadir el botón a la derecha

        JPanel panelBotones = new JPanel();
        if (!rol.equals("Medicos")) {
            panelBotones.add(btnMensajeGrupo);
        }
        panelBotones.add(btnMensajeAdministracion);
        panelBotones.add(btnCrearGrupo);
        panelBotones.add(btnAgregarMiembros);

        contenedor.add(scroll, BorderLayout.CENTER);
        contenedor.add(panel, BorderLayout.SOUTH);
        contenedor.add(panelBotones, BorderLayout.NORTH);
        contenedor.add(scrollUsuarios, BorderLayout.EAST);

        // Agregar acción para el botón "Limpiar Chat"
        btnLimpiarChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(""); // Limpiar el JTextArea
            }
        });
    }

    public void addActionListener(ActionListener accion) {
        textField.addActionListener(accion);
        boton.addActionListener(accion);
        btnCrearGrupo.addActionListener(accion);
        btnMensajeGrupo.addActionListener(accion);
        btnAgregarMiembros.addActionListener(accion);
        btnMensajeAdministracion.addActionListener(accion);
        // Agregar el listener para el botón "Limpiar Chat" si se necesita
        btnLimpiarChat.addActionListener(accion);
    }

    public void addTexto(String texto) {
        textArea.append(texto);
    }

    public String getTexto() {
        String texto = textField.getText();
        textField.setText("");
        return texto;
    }

    public void addUsuarioTexto(String texto) {
        textAreaUsuarios.append(texto);
    }

    public void cambiartextAreaUsuarios(String rol) {
        StringBuilder texto = new StringBuilder();
        texto.append("GRUPOS:\n\n");
        List<Grupo> grupos = ServidorChat.getListaGrupos();
        for (Grupo grupo : grupos) {
            String nombreGrupo = grupo.getNombreGrupo();
            //List<String> usuariosDelGrupo = grupo.getNombresMiembros();
            List<HiloDeCliente> listaUsuarios = grupo.getListaMiembros();
            for (HiloDeCliente usuario : listaUsuarios) {
                if (rol.equals("Medicos") && usuario.getRol().equals("Auxiliar")) {
                } else {
                    texto.append(nombreGrupo).append(":\n");
                    texto.append("   ").append(usuario.nombreUsuario).append("\n");
                }
            }
            texto.append("\n");
        }
        if (texto.isEmpty()) {
        } else {
            textAreaUsuarios.setText("");
            textAreaUsuarios.append(texto.toString());
        }
    }
}
