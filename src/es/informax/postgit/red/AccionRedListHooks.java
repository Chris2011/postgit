package es.informax.postgit.red;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author daniel.vazquez
 */
public class AccionRedListHooks implements AccionRed {

    @Override
    public Map<String, String> getParametros() {
        HashMap<String, String> params = new HashMap<>();
        
        return params;
    }

    @Override
    public EventoRed parsearRespuesta(String respuesta) {
        Object resultado = null;
        
        try {
            JSONParser parser = new JSONParser();
            resultado = parser.parse(respuesta);
            if(!(resultado instanceof Collection)) {
                Object resultadoTemp = resultado;
                
                resultado = new JSONArray();
                if(resultadoTemp instanceof Map) {
                    ((Collection)resultado).add(resultadoTemp);
                }
            }
        }
        catch(ParseException ex) { }
        
        return new EventoRed(this, true, resultado, null);
    }

    @Override
    public TransmisorHTTP getTransmisor(String urlServidor, String ipProxy, int puertoProxy, String usuario, String pass) {
        return new TransmisorHTTPTexto(urlServidor, ipProxy, puertoProxy, usuario, pass);
    }
}
