
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

        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese la cantidad de clientes que desea abrir: ");
        int numClientes = scanner.nextInt();
        
        for(int i = 0 ; i < numClientes ; i++){
            new ClienteChat();
        }
    }
    public ClienteChat(){
        try{
            socket = new Socket("localhost", 5000);
            ControlCliente control = new ControlCliente(socket);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}