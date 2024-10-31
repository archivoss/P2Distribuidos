import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 *
 * @author RPVZ
 */
public class PanelCliente extends JPanel{
    private JScrollPane scroll;
    private JTextArea textArea;
    private JTextField textField;
    private JButton boton;
    private JButton btnUsuarios;

    public PanelCliente(Container contenedor){
        contenedor.setLayout(new BorderLayout());
        textArea = new JTextArea();
        scroll = new JScrollPane(textArea);

        JPanel panel = new JPanel(new BorderLayout());
        textField = new JTextField(50);
        boton = new JButton("Enviar");
        btnUsuarios = new JButton("Ver Usuarios");

        panel.add(textField, BorderLayout.NORTH);
        panel.add(boton, BorderLayout.SOUTH);
        panel.add(btnUsuarios, BorderLayout.WEST);

        contenedor.add(scroll, BorderLayout.CENTER);
        contenedor.add(panel, BorderLayout.SOUTH);

        btnUsuarios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
    }

    

    public void addActionListener(ActionListener accion){
        textField.addActionListener(accion);
        boton.addActionListener(accion);
        btnUsuarios.addActionListener(accion);
    }
    public void addTexto(String texto){
        textArea.append(texto);
    }
    public String getTexto(){
        String texto = textField.getText();
        textField.setText("");
        return texto;
    }
}
