import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 *
 * @author RPVZ
 */
public class PanelCliente extends JPanel {
    private JScrollPane scroll;
    private JScrollPane scrollUsuarios; // Nuevo JScrollPane

    private JTextArea textArea;
    private JTextArea textAreaUsuarios; // Nuevo JTextArea para el listado de usuarios
    private JTextField textField;
    private JButton boton;
    private JButton btnUsuarios;

    public PanelCliente(Container contenedor) {
        contenedor.setLayout(new BorderLayout());
        
        textArea = new JTextArea();
        scroll = new JScrollPane(textArea);
        
        // Configuración del nuevo área de texto y scroll para los usuarios
        textAreaUsuarios = new JTextArea();
        textAreaUsuarios.setEditable(false);
        scrollUsuarios = new JScrollPane(textAreaUsuarios);
        scrollUsuarios.setPreferredSize(new Dimension(200, 0)); // Ancho fijo de 150 px, altura flexible


        JPanel panel = new JPanel(new BorderLayout());
        textField = new JTextField(50);
        boton = new JButton("Enviar");
        btnUsuarios = new JButton("Ver Usuarios");
        // Cambia los colores de los botones a colores pastel
        boton.setBackground(new Color(173, 216, 230)); // Color azul pastel
        boton.setForeground(Color.BLACK);               // Texto en negro

        btnUsuarios.setBackground(new Color(255, 182, 193)); // Color rosa pastel
        btnUsuarios.setForeground(Color.BLACK);           // Texto en negro
        panel.add(textField, BorderLayout.NORTH);
        panel.add(boton, BorderLayout.CENTER);
        panel.add(btnUsuarios, BorderLayout.WEST);

        contenedor.add(scroll, BorderLayout.CENTER);
        contenedor.add(panel, BorderLayout.SOUTH);
        
        // Agregar el nuevo JScrollPane en el lado derecho
        contenedor.add(scrollUsuarios, BorderLayout.EAST);

        btnUsuarios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Aquí puedes añadir la lógica para cargar los usuarios
            }
        });
    }

    public void addActionListener(ActionListener accion) {
        textField.addActionListener(accion);
        boton.addActionListener(accion);
        btnUsuarios.addActionListener(accion);
    }

    public void addTexto(String texto) {
        textArea.append(texto);
    }

    public String getTexto() {
        String texto = textField.getText();
        textField.setText("");
        return texto;
    }

    // Método adicional para agregar texto al área de usuarios
    public void addUsuarioTexto(String texto) {
        textAreaUsuarios.append(texto);
    }
}
