package es.informax.postgit.red;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author daniel.vazquez
 * @version 1.3
 */
public class TransmisorHTTPFichero extends TransmisorHTTP {

    private final File ficheroDestino;

    public TransmisorHTTPFichero(String url, File ficheroDestino) {
        super(url);

        this.ficheroDestino = ficheroDestino;
    }

    public TransmisorHTTPFichero(String url, File ficheroDestino, String ipProxy, int puertoProxy, String usuario, String pass) {
        super(url, ipProxy, puertoProxy, usuario, pass);

        this.ficheroDestino = ficheroDestino;
    }

    /**
     * Se descaga un archivo y lo graba en la ruta especificada.
     *
     * @param variables
     * @return Devuelve la cantidad de bytes descargados.
     * @throws java.io.IOException
     */
    @Override
    public String transmitir(Map<String, String> variables) throws IOException {
        long bytes = 0;

        try {
            InputStream is = comunicarse(variables);
            FileOutputStream fos = new FileOutputStream(ficheroDestino);

            byte[] array = new byte[1000];
            int leido = is.read(array);
            while (leido > 0) {
                bytes += leido;

                fos.write(array, 0, leido);
                leido = is.read(array);
            }

            is.close();
            fos.close();
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            connection.disconnect();
        }

        return String.valueOf(bytes);
    }
}
