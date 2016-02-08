package es.informax.postgit.red;

import java.util.HashMap;
import java.util.Map;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author daniel.vazquez
 */
public class AccionRedAgregarHook implements AccionRed {
    
    private final String urlHook;

    public AccionRedAgregarHook(String urlHook) {
        this.urlHook = urlHook;
    }

    @Override
    public Map<String, String> getParametros() {
        HashMap<String, String> params = new HashMap<>();
        params.put("url", urlHook);
        params.put("tag_push_events", "1");
        params.put("push_events", "0");

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
