import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
/**
 *
 * @author RPVZ
 */
public class HiloDeCliente implements Runnable, ListDataListener{
    private DefaultListModel mensajes;
    private Socket socket;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private String rol;
    private String contrasena;
    public String nombreUsuario;
    public LocalDateTime horaConexion;
    public ArrayList<String> listaMensajes;


    public HiloDeCliente(DefaultListModel mensajes, Socket socket, String nombreUsuario, String rol, String contrasena){
        this.mensajes = mensajes;
        this.socket = socket;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.contrasena = contrasena;
        this.horaConexion = LocalDateTime.now();
        this.listaMensajes= new ArrayList<>();

        try{
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
            mensajes.addListDataListener(this);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            while (true) {
                String texto = dataInput.readUTF();
                System.out.println("Mensaje recibido: " + texto);
                
                // Validación para evitar errores si el texto está vacío
                if (texto == null || texto.isEmpty()) {
                    continue; // Si es nulo o vacío, ignoramos y seguimos esperando nuevos mensajes
                }

                //MENSAJE PRIVADO
                if (texto.charAt(0) == '@') {
                    List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                    String[] partes = texto.split(":", 2);

                    // Validación para evitar problemas si no se pudo dividir el texto correctamente
                    if (partes.length < 2) {
                        dataOutput.writeUTF("Formato de mensaje inválido.");
                        continue;
                    }

                    String nombreDestinatario = partes[0].substring(1); // Quitamos el '@'
                    String mensaje = partes[1];
                    this.listaMensajes.add(nombreDestinatario);
                    boolean usuarioEncontrado = false;

                    // Buscamos al destinatario en la lista de hilos
                    
                    for (HiloDeCliente hilo : lista) {
                        if (hilo.getNombreUsuario().equals(nombreDestinatario)) {
                            try {
                                dataOutput.writeUTF("[Mensaje privado enviado a " + nombreDestinatario + "]: " + mensaje);
                                hilo.mensajePrivado(nombreUsuario, mensaje, hilo);
                                usuarioEncontrado = true;
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    /*if (!usuarioEncontrado) {
                        dataOutput.writeUTF("El usuario " + nombreDestinatario + " no se encuentra conectado.");
                    }*/
                }

                if(texto.startsWith("@mensajeadministracion:")){
                    try {
                        // Extraer la parte después de ":"
                        String[] partes = texto.split(":", 2);
                        String mensaje = partes[1];

                        List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                        List<Grupo> listaGrupos = ServidorChat.getListaGrupos();
                        List<String> gruposAdministracion = Arrays.asList("Medicos","Admision", "Pabellon", "Examenes");

                        for (Grupo grupo : listaGrupos) {
                            if (gruposAdministracion.contains(grupo.getNombreGrupo())) {
                                for (HiloDeCliente miembro : grupo.getListaMiembros()) {
                                    if(grupo.getNombreGrupo().equals("Medicos")){
                                        for(HiloDeCliente hilo : lista){
                                            if(hilo.getNombreUsuario().equals(miembro.getNombreUsuario())){
                                                try {
                                                    hilo.dataOutput.writeUTF("[Mensaje enviado a Administración]: " + mensaje);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        
                                    }
                                    else{
                                        for(HiloDeCliente hilo : lista){
                                            if(hilo.getNombreUsuario().equals(miembro.getNombreUsuario())){
                                                try {
                                                    hilo.dataOutput.writeUTF("[Mensaje grupo (ADMINISTRACIÓN)]: " + mensaje);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(texto.startsWith("@mensajegrupo:")){
                    try {
                        // Extraer la parte después de ":"
                        String[] partes = texto.split(":", 2);

                        List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                        List<Grupo> listaGrupos = ServidorChat.getListaGrupos();

                        if (partes.length < 2) {
                            dataOutput.writeUTF("Error: Formato incorrecto. Usa '@mensajegrupo: nombreGrupo, mensaje'");
                            return;
                        }

                        // Separar el nombre del grupo y el mensaje
                        String[] datos = partes[1].split(",", 2);
                        String nombreGrupo = datos[0].trim(); // El primer elemento es el nombre del grupo
                        String mensaje = datos[1].trim(); // El segundo elemento es el mensaje

                        // Buscar el grupo en la lista de grupos
                        for (Grupo grupo : listaGrupos) {
                            if (grupo.getNombreGrupo().equals(nombreGrupo)) {
                                for (HiloDeCliente miembro : grupo.getListaMiembros()) {
                                    try {
                                        for(HiloDeCliente hilo : lista){
                                            if(hilo.getNombreUsuario().equals(miembro.getNombreUsuario())){
                                                hilo.dataOutput.writeUTF("[Mensaje del grupo (" + nombreGrupo + ")]: " + mensaje);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            }
                        }
                        dataOutput.writeUTF("[Mensaje enviado al grupo (" + nombreGrupo + ")]: " + mensaje);
                    } catch (IOException e) {
                        System.err.println("Error al enviar mensaje de confirmación: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error al enviar mensaje de grupo: " + e.getMessage());
                    }
                }

                if(texto.startsWith("@agregarmiembros:")){

                    try {
                        String [] partes = texto.split(":", 2);
                        boolean existe = false;
                        Grupo grupoAux = null;

                        // Separar el nombre del grupo y los usuarios
                        String[] datos = partes[1].split(",", 2);
                        String nombreGrupo = datos[0].trim(); // El primer elemento es el nombre del grupo

                        // Verificar si hay usuarios
                        String[] usuarios = (datos.length > 1) ? datos[1].split(",") : new String[0];
                        int count = 0;

                        List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                        List<Grupo> listaGrupos = ServidorChat.getListaGrupos();

                        // Verificar si el grupo existe
                        for (Grupo grupo : listaGrupos) {
                            if(grupo.getNombreGrupo().equals(nombreGrupo)){
                                grupoAux = grupo;
                                existe = true;
                                break;
                            }
                        }
                        if(existe == true){
                            // Verificar si los usuarios están conectados
                            for (int i = 0; i < usuarios.length; i++) {
                                for (int j = 0; j < lista.size() ; j++) {
                                    if(lista.get(j).getNombreUsuario().equals(usuarios[i])){
                                        count++;
                                        break;
                                    } 
                                    if( j == lista.size() - 1){
                                        dataOutput.writeUTF("El usuario " + usuarios[i] + " no se encuentra conectado.");
                                        return;
                                    }
                                }
                            }
                        }
                        else{
                            dataOutput.writeUTF("El grupo " + nombreGrupo + " no existe.");
                        }

                        if(count >= 1){
                            for (int i = 0; i < usuarios.length; i++) {
                                for (int j = 0; j < lista.size() ; j++) {
                                    if(lista.get(j).getNombreUsuario().equals(usuarios[i])){
                                        grupoAux.agregarMiembro(lista.get(j));
                                    }
                                }
                            }
                            dataOutput.writeUTF("[Usuarios agregados con éxito.]");
                        }


                            
                    } catch (IOException e) {
                        System.err.println("Error al enviar mensaje de confirmación: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error al crear el grupo: " + e.getMessage());
                    }

                    
                } 
                
                if (texto.startsWith("@creargrupo:")) {
                    try {
                        // Extraer la parte después de ":"
                        String[] partes = texto.split(":", 2);

                        List<HiloDeCliente> lista = ServidorChat.getListaHilos();



                        if (partes.length < 2) {
                            dataOutput.writeUTF("Error: Formato incorrecto. Usa '@creargrupo: nombreGrupo, usuario1, usuario2, ...'");
                            return;
                        }

                        // Separar el nombre del grupo y los usuarios
                        String[] datos = partes[1].split(",", 2);
                        String nombreGrupo = datos[0].trim(); // El primer elemento es el nombre del grupo

                        // Verificar si hay usuarios
                        String[] usuarios = (datos.length > 1) ? datos[1].split(",") : new String[0];
                        int count = 0;

                        
                        // Verificar si los usuarios están conectados
                        for (int i = 0; i < usuarios.length; i++) {
                            for (int j = 0; j < lista.size() ; j++) {
                                if(lista.get(j).getNombreUsuario().equals(usuarios[i])){
                                    count++;
                                    break;
                                } 
                                if( j == lista.size() - 1){
                                    dataOutput.writeUTF("[El usuario " + usuarios[i] + " no se encuentra conectado.]");
                                    return;
                                }
                            }
                        }

                       
                        if(count == usuarios.length){
                            Grupo grupo = new Grupo(nombreGrupo);
                            for (int i = 0; i < usuarios.length; i++) {
                                for (int j = 0; j < lista.size() ; j++) {
                                    if(lista.get(j).getNombreUsuario().equals(usuarios[i])){
                                        grupo.agregarMiembro(lista.get(j));
                                    }
                                }
                            }
                            ServidorChat.agregarGrupo(grupo);
                        }

                        dataOutput.writeUTF("[Grupo creado con éxito.]");


                    } catch (IOException e) {
                        System.err.println("Error al enviar mensaje de confirmación: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error al crear el grupo: " + e.getMessage());
                    }
                }
                

                if(texto.equals("/usuarios")){
                    List<HiloDeCliente> lista = ServidorChat.getListaHilos();
                    String usuarios = "Usuarios conectados: ";
                    for (HiloDeCliente hilo : lista){
                        usuarios += hilo.getNombreUsuario() + ", ";
                    }
                    dataOutput.writeUTF(usuarios);
                }
                if(texto.equals("/desconectar")){
                    
                    dataOutput.writeUTF("Desconectado del servidor.");
                    dataOutput.writeUTF("/desconectar");
                    desconectar();
                    break;
                }
                
                //MENSAJE GENERAL
                if(texto.charAt(0) != '@' && texto.charAt(0) != '/'){ // Si no es un mensaje privado, lo enviamos a todos los clientes
                    mensajes.addElement(nombreUsuario + ": " + texto);
                    this.listaMensajes.add("general");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private void mensajePrivado (String nombreDestinatario, String mensaje, HiloDeCliente hilo){    
        try{
            hilo.dataOutput.writeUTF("[Mensaje privado de " + nombreDestinatario + "]: " + mensaje);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getNombreUsuario(){
        return nombreUsuario;
    }

    private void desconectar() {
        try {
            ServidorChat.eliminarCliente(this);
            socket.close();
            System.out.println(nombreUsuario + " se ha desconectado.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRol(){
        return rol;
    }

    public void setRol(String rol){
        this.rol = rol;
    }

    public String getContrasena(){
        return contrasena;
    }

    public ArrayList<String> getlistaMensajes(){
        return this.listaMensajes;
    }

    @Override
    public void intervalAdded(ListDataEvent e){
        String texto = (String) mensajes.getElementAt(e.getIndex0());
        try{
            dataOutput.writeUTF(texto);
        } catch (Exception excepcion){
            excepcion.printStackTrace();
        }
    }
    
    // Método para calcular el tiempo conectado
    public Duration tiempoConectado() {
        return Duration.between(horaConexion, LocalDateTime.now());
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
