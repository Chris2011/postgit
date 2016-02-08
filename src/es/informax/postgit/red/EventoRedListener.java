package es.informax.postgit.red;

import java.util.EventListener;

/**
 *
 * @author daniel.vazquez
 */
public interface EventoRedListener extends EventListener {
    public void comunicacionCompletada(EventoRed evt);
}
