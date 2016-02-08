package es.informax.postgit.red;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Clase para intercambiar datos estilo web.
 *
 * @author bardecho
 * @version 1.5
 */
public abstract class TransmisorHTTP {

    private String url, usuario, pass, requestMethod;
    private final Proxy proxy;
    protected HttpURLConnection connection;
    private CookieManager cookiesEnviables, cookiesRecibidas;

    /**
     *
     * @param url
     */
    public TransmisorHTTP(String url) {
        this.url = url;
        this.proxy = Proxy.NO_PROXY;
    }

    /**
     *
     * @param url
     * @param ipProxy Si es null o cadena vacía, no se utiliza proxy.
     * @param puertoProxy Si es 0 no se utiliza proxy.
     * @param usuario Si es null no se utiliza autenticación http.
     * @param pass Si es null no se utiliza autenticación http.
     */
    public TransmisorHTTP(String url, String ipProxy, int puertoProxy, String usuario, String pass) {
        this.url = url;
        if (ipProxy != null && !ipProxy.isEmpty() && puertoProxy > 0) {
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipProxy, puertoProxy));
        }
        else {
            this.proxy = Proxy.NO_PROXY;
        }
        this.usuario = usuario;
        this.pass = pass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Envía un mensaje a un servidor y abre un recurso a su respuesta.
     *
     * @param variables
     * @return
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     */
    protected InputStream comunicarse(Map<String, String> variables) throws UnsupportedEncodingException, MalformedURLException, IOException {
        connection = null;
        InputStream is = null;
        String parametrosString = "";
        if (!variables.isEmpty()) {
            StringBuilder parametros = new StringBuilder();
            for (String clave : variables.keySet()) {
                parametros.append("&");
                parametros.append(clave);
                parametros.append("=");
                parametros.append(URLEncoder.encode(variables.get(clave), "UTF-8"));
            }
            parametrosString = parametros.substring(1);
        }

        //Creamos la conexión y seguimos las redirecciones
        String urlTemp = this.url;
        for (int intentos = 0; intentos < 100; intentos++) {
            connection = (HttpURLConnection) new URL(urlTemp).openConnection(proxy);
            connection.setRequestMethod((requestMethod != null && !requestMethod.isEmpty() ? requestMethod : "POST"));
            connection.setRequestProperty("User-Agent", "Informax");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if(cookiesEnviables != null && cookiesEnviables.getCookieStore().getCookies().size() > 0) {
                connection.setRequestProperty("Cookie", join(";", cookiesEnviables.getCookieStore().getCookies()));
            }
            else {
                connection.setRequestProperty("Cookie", "");
            }

            //Credenciales http básicas
            if (this.usuario != null && this.pass != null) {
                String encodeToString = Base64.getEncoder().encodeToString((usuario + ":" + pass).getBytes());
                connection.setRequestProperty("Authorization", "Basic " + encodeToString);
            }

            //Añadimos los datos
            connection.setRequestProperty("Content-Length", Integer.toString(parametrosString.getBytes().length));
            connection.setRequestProperty("Content-Language", "es-ES");

            //Configuración
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setReadTimeout(60000);
            connection.setConnectTimeout(60000);

            //Enviamos
            if(!connection.getRequestMethod().equalsIgnoreCase("GET")) {
                connection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(parametrosString);
                wr.flush();
                wr.close();
            }

            //Recibimos la respuesta
            is = connection.getInputStream();
            int status = connection.getResponseCode();
            if (status >= 300 && status <= 399) {
                urlTemp = connection.getHeaderField("Location");
            }
            else {
                break;
            }
        }

        almacenarCookiesRecibidas();

        return is;
    }

    public abstract String transmitir(Map<String, String> variables) throws IOException;
    
    /**
     * Guarda las cookies recibidas.
     */
    private void almacenarCookiesRecibidas() {
        CookieManager cookieManager = new CookieManager();

        if(connection != null) {
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            if(cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }               
            }
        }
        
        cookiesRecibidas = cookieManager;
    }

    /**
     * Devuelve un contenedor con las cookies de la respuesta.
     * @return 
     */
    public CookieManager getCookiesRecibidas() {
        return cookiesRecibidas;
    }
    
    /**
     * Modifica las cookies para el envío.
     * @param cookieManager 
     */
    public void setCookies(CookieManager cookieManager) {
        cookiesEnviables = cookieManager;
    }

    /**
     * Devuelve las cookies para el envío.
     * @return 
     */
    public CookieManager getCookiesEnviables() {
        return cookiesEnviables;
    }
    
    /**
     * Une un List de Cookies.
     * @param glue
     * @param parts
     * @return 
     */
    private String join(String glue, List<HttpCookie> parts) {
        StringBuilder result = new StringBuilder();
        for(HttpCookie cookie : parts) {
            result.append(cookie.toString());
            result.append(glue);
        }
        if(result.length() > 0) {
            result.delete(result.length() - glue.length(), result.length());
        }
        
        return result.toString();
    }
}
