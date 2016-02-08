package es.informax.postgit.red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 *
 * @author daniel.vazquez
 * @version 1.4
 */
public class TransmisorHTTPTexto extends TransmisorHTTP {

    public TransmisorHTTPTexto(String url) {
        super(url);
    }

    public TransmisorHTTPTexto(String url, String ipProxy, int puertoProxy, String usuario, String pass) {
        super(url, ipProxy, puertoProxy, usuario, pass);
    }

    /**
     * Envía un mensaje por post y recibe un string como respuesta.
     *
     * @param variables Puede estar vacío.
     * @return
     * @throws java.io.IOException
     */
    @Override
    public String transmitir(Map<String, String> variables) throws IOException {
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(comunicarse(variables), "UTF-8"));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();

            return response.toString();
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            connection.disconnect();
        }
    }
}
