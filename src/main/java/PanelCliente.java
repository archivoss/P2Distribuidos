import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author RPVZ
 */
public class PanelCliente extends JPanel {
    private JScrollPane scroll;
    private JTextPane textArea;
    private JTextField textField;
    private JButton boton;
    private JButton btnLimpiarChat; // Nuevo botón para limpiar chat
    private JButton btnCrearGrupo;
    private JButton btnMensajeGrupo;
    private JButton btnAgregarMiembros;
    private JButton btnMensajeAdministracion;
    private JButton btnTodos;
    private JButton btnemergencia;
    private JScrollPane scrollUsuarios;
    private JTextArea textAreaUsuarios;

    public PanelCliente(Container contenedor, String rol) {
        contenedor.setLayout(new BorderLayout());

        textArea = new JTextPane();
        textArea.setEditorKit(new HTMLEditorKit()); // Configurar JTextPane para aceptar HTML
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
        btnTodos = new JButton("Todos");
        btnemergencia = new JButton("Emergencia");

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
        btnTodos.setBackground(botonesColor);

        boton.setForeground(Color.black);
        btnLimpiarChat.setForeground(Color.black); // Color del texto del nuevo botón
        btnAgregarMiembros.setForeground(Color.black);
        btnMensajeAdministracion.setForeground(Color.black);
        btnCrearGrupo.setForeground(Color.black);
        btnMensajeGrupo.setForeground(Color.black);
        btnTodos.setForeground(Color.black);

        panel.add(textField, BorderLayout.NORTH);
        panel.add(boton, BorderLayout.CENTER);
        panel.add(btnLimpiarChat, BorderLayout.EAST); // Añadir el botón a la derecha

        if (rol.equals("Admin")) {
            JPanel panelBotonesAdmin = new JPanel();
            panelBotonesAdmin.add(btnemergencia);
            contenedor.add(panelBotonesAdmin, BorderLayout.NORTH);
            contenedor.add(scroll, BorderLayout.CENTER);
            contenedor.add(panel, BorderLayout.SOUTH);
        } else {
            JPanel panelBotones = new JPanel();
            if (!rol.equals("Medicos")) {
                panelBotones.add(btnMensajeGrupo);
            }
            panelBotones.add(btnMensajeAdministracion);
            panelBotones.add(btnTodos);
            panelBotones.add(btnCrearGrupo);
            panelBotones.add(btnAgregarMiembros);

            contenedor.add(scroll, BorderLayout.CENTER);
            contenedor.add(panel, BorderLayout.SOUTH);
            contenedor.add(panelBotones, BorderLayout.NORTH);
        }

        contenedor.add(scrollUsuarios, BorderLayout.EAST);

        // Agregar acción para el botón "Limpiar Chat"
        btnLimpiarChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(""); // Limpiar el JTextPane
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
        btnTodos.addActionListener(accion);
        // No es necesario agregar un listener para el botón "Limpiar Chat" aquí,
        // ya que se definió en el constructor.
    }

    public void addTexto(String texto) {
        // Usa HTMLEditorKit y HTMLDocument para insertar texto enriquecido sin
        // sobrescribir contenido anterior
        HTMLDocument doc = (HTMLDocument) textArea.getDocument();
        HTMLEditorKit kit = (HTMLEditorKit) textArea.getEditorKit();

        try {
            // Limpiar el contenido vacío de textArea
            if (texto != null && !texto.trim().isEmpty()) {
                kit.insertHTML(doc, doc.getLength(), "<p>" + texto + "</p>", 0, 0, null);
            }
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
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
            List<HiloDeCliente> listaUsuarios = grupo.getListaMiembros();
            for (HiloDeCliente usuario : listaUsuarios) {
                if (rol.equals("Medicos") && usuario.getRol().equals("Auxiliar")) {
                    // No hacer nada si el rol es Medicos y el usuario es Auxiliar
                } else {
                    texto.append(nombreGrupo).append(":\n");
                    texto.append("   ").append(usuario.nombreUsuario).append("\n");
                }
            }
            texto.append("\n");
        }
        if (texto.length() > 0) {
            textAreaUsuarios.setText(texto.toString());
        }
    }
}
