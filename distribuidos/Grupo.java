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
    
    
}
