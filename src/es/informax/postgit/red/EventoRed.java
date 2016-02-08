package es.informax.postgit.red;

import java.util.EventObject;

/**
 *
 * @author daniel.vazquez
 */
public class EventoRed extends EventObject {
    private final Object extra, resultado;
    private final boolean correcto;

    /**
     *
     * @param source
     * @param correcto
     * @param extra
     */
    public EventoRed(Object source, boolean correcto, Object extra) {
        this(source, correcto, null, extra);
    }
    
    /**
     *
     * @param source
     * @param correcto Estado de la acción.
     * @param resultado Los datos del resultado.
     * @param extra Algún dato adicional.
     */
    public EventoRed(Object source, boolean correcto, Object resultado, Object extra) {
        super(source);

        this.extra = extra;
        this.correcto = correcto;
        this.resultado = resultado;
    }

    public Object getExtra() {
        return extra;
    }

    public Object getResultado() {
        return resultado;
    }

    public boolean isCorrecto() {
        return correcto;
    }
    
}
