package es.informax.postgit.red;

import java.util.Map;

/**
 * La clase que lo implementa se utiliza como acción para el comunicador.
 * @author daniel.vazquez
 * @version 1.2
 */
public interface AccionRed {
    /**
     * Devuelve el transmisor para intercambiar los datos.
     * @param urlServidor
     * @param ipProxy
     * @param puertoProxy
     * @param usuario
     * @param pass
     * @return 
     */
    public TransmisorHTTP getTransmisor(String urlServidor, String ipProxy, int puertoProxy, String usuario, String pass);
    
    /**
     * Devuelve los parámetros para enviar por red.
     * @return 
     */
    public Map<String, String> getParametros();
    
    /**
     * Parsea la respuesta recibida y la devuelve en un evento de red.
     * @param respuesta
     * @return 
     */
    public EventoRed parsearRespuesta(String respuesta);
}
