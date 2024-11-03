
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.util.Scanner;

/**
 * @author RPVZ
 */
public class ClienteChat{
    private Socket socket;
    private PanelCliente panel;

    public static void main(String[] args){

    }
    public ClienteChat(String nombreUsuario, String rol){
        try{
            socket = new Socket("localhost", 5000);
            ControlCliente control = new ControlCliente(socket, nombreUsuario, rol);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}