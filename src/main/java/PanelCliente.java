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
    private JButton btnCrearGrupo;
    private JButton btnMensajeGrupo;
    private JButton btnAgregarMiembros;
    private JButton btnMensajeAdministracion;

    public PanelCliente(Container contenedor){
        contenedor.setLayout(new BorderLayout());
        textArea = new JTextArea();
        scroll = new JScrollPane(textArea);

        JPanel panel = new JPanel(new BorderLayout());
        textField = new JTextField(50);
        boton = new JButton("Enviar");
        btnUsuarios = new JButton("Ver Usuarios");
        btnCrearGrupo = new JButton("Crear Grupo");
        btnMensajeGrupo = new JButton("Mensaje Grupo");
        btnAgregarMiembros = new JButton("Agregar Miembros");
        btnMensajeAdministracion = new JButton("Mensaje Administración");

        panel.add(textField, BorderLayout.NORTH);
        panel.add(boton, BorderLayout.SOUTH);
        panel.add(btnUsuarios, BorderLayout.WEST);

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnCrearGrupo);
        panelBotones.add(btnMensajeGrupo);
        panelBotones.add(btnAgregarMiembros);
        panelBotones.add(btnMensajeAdministracion);

        contenedor.add(scroll, BorderLayout.CENTER);
        contenedor.add(panel, BorderLayout.SOUTH);
        contenedor.add(panelBotones, BorderLayout.NORTH);

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
        btnCrearGrupo.addActionListener(accion);
        btnMensajeGrupo.addActionListener(accion);
        btnAgregarMiembros.addActionListener(accion);
        btnMensajeAdministracion.addActionListener(accion);
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
