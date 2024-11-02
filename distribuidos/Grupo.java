import java.util.ArrayList;
import java.util.List;

public class Grupo {
    private String nombreGrupo;
    private List<HiloDeCliente> listaMiembros = new ArrayList<>();
    private List<String> mensajes = new ArrayList<>();

    public Grupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }

    public void agregarMiembro(HiloDeCliente miembro) {
        listaMiembros.add(miembro);
    }

    public void eliminarMiembro(HiloDeCliente miembro) {
        listaMiembros.remove(miembro);
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }
    public void setListaMiembros(List<HiloDeCliente> listaMiembros) {
        this.listaMiembros = listaMiembros;
    }

    public List<String> getMensajes() {
        return mensajes;
    }

    public void setMensajes(String mensajes) {
        this.mensajes.add(mensajes);
    }
    public List<HiloDeCliente> getListaMiembros() {
        return listaMiembros;
    }
    // Método para obtener los nombres de los miembros
    public List<String> getNombresMiembros() {
        List<String> nombres = new ArrayList<>();
        for (HiloDeCliente miembro : listaMiembros) {
            nombres.add(miembro.getNombreUsuario()); // Asegúrate de que getNombreUsuario() esté implementado en HiloDeCliente
        }
        return nombres;
    }
    
}
