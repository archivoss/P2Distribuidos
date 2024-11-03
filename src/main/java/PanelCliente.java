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
    private JButton btnTodos;
    private JButton btnemergencia;

    public PanelCliente(Container contenedor, String rol){
        contenedor.setLayout(new BorderLayout());
        textArea = new JTextArea();
        scroll = new JScrollPane(textArea);

        JPanel panel = new JPanel(new BorderLayout());
        textField = new JTextField(50);
        boton = new JButton("Enviar");
        btnUsuarios = new JButton("Ver Usuarios");
        btnAgregarMiembros = new JButton("Agregar Miembros");
        btnMensajeAdministracion = new JButton("Mensaje Administración");
        btnCrearGrupo = new JButton("Crear Grupo");
        btnMensajeGrupo = new JButton("Mensaje Grupo");
        btnTodos = new JButton("Todos"); 
        btnemergencia = new JButton("Emergencia");

        panel.add(textField, BorderLayout.NORTH);
        panel.add(boton, BorderLayout.SOUTH);
        panel.add(btnUsuarios, BorderLayout.WEST);

        if(rol.equals("Admin")){
            JPanel panelBotonesAdmin = new JPanel(); 
            panelBotonesAdmin.add(btnemergencia);
            contenedor.add(panelBotonesAdmin, BorderLayout.NORTH);
            contenedor.add(scroll, BorderLayout.CENTER);
            contenedor.add(panel, BorderLayout.SOUTH);
        }else{
            JPanel panelBotones = new JPanel();
            if(!rol.equals("Medicos")){
                panelBotones.add(btnMensajeGrupo);
            }
            panelBotones.add(btnMensajeAdministracion);
            panelBotones.add(btnTodos);
            panelBotones.add(btnCrearGrupo);
            panelBotones.add(btnAgregarMiembros);
            
            
            contenedor.add(scroll, BorderLayout.CENTER);
            contenedor.add(panel, BorderLayout.SOUTH);
            contenedor.add(panelBotones, BorderLayout.NORTH);
    
            btnUsuarios.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    
                }
            });
        }
               

       
    }

    

    public void addActionListener(ActionListener accion){
        textField.addActionListener(accion);
        boton.addActionListener(accion);
        btnUsuarios.addActionListener(accion);
        btnCrearGrupo.addActionListener(accion);
        btnMensajeGrupo.addActionListener(accion);
        btnAgregarMiembros.addActionListener(accion);
        btnMensajeAdministracion.addActionListener(accion);
        btnTodos.addActionListener(accion);
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
