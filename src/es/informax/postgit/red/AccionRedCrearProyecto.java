package es.informax.postgit.red;

import java.util.HashMap;
import java.util.Map;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author daniel.vazquez
 */
public class AccionRedCrearProyecto implements AccionRed {
    
    private final String nombreProyecto, descripcion;
    private final long visibilidad;

    public AccionRedCrearProyecto(String nombreProyecto, String descripcion, long visibilidad) {
        this.nombreProyecto = nombreProyecto;
        this.descripcion = descripcion;
        this.visibilidad = visibilidad;
    }
    
    @Override
    public Map<String, String> getParametros() {
        HashMap<String, String> params = new HashMap<>();
        params.put("name", nombreProyecto);
        params.put("description", descripcion);
        params.put("visibility_level", String.valueOf(visibilidad));

        return params;
    }

    @Override
    public EventoRed parsearRespuesta(String respuesta) {
        Object resultado = null;
        
        try {
            JSONParser parser = new JSONParser();
            resultado = parser.parse(respuesta);
        }
        catch(ParseException ex) { }
        
        return new EventoRed(this, true, resultado, null);
    }

    @Override
    public TransmisorHTTP getTransmisor(String urlServidor, String ipProxy, int puertoProxy, String usuario, String pass) {
        return new TransmisorHTTPTexto(urlServidor, ipProxy, puertoProxy, usuario, pass);
    }
}
