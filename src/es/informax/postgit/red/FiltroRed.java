package es.informax.postgit.red;

import java.util.List;
import java.util.Objects;

/**
 * Filtro para Comunicador.
 * @author daniel.vazquez
 * @version 1.2
 */
public class FiltroRed {
    private final String nombre;
    private String valor;

    public FiltroRed(String nombre, String valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getNombre() {
        return nombre;
    }
    
    /**
     * Busca el filtro deseado en una lista.
     * @param filtros
     * @param nombre
     * @return 
     */
    public static FiltroRed buscar(List<FiltroRed> filtros, String nombre) {
        FiltroRed filtroBuscado = null;
        
        for(FiltroRed filtro : filtros) {
            if(filtro.getNombre().equals(nombre)) {
                filtroBuscado = filtro;
                break;
            }
        }
        
        return filtroBuscado;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.nombre);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FiltroRed other = (FiltroRed) obj;
        return Objects.equals(this.nombre, other.nombre);
    }
    
}
