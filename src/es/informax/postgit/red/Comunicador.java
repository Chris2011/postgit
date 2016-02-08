package es.informax.postgit.red;

import java.io.IOException;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Sirve para comunicarse con un servidor.
 *
 * @author daniel.vazquez
 * @version 1.7
 */
public class Comunicador implements Runnable {

    private String urlServidor, ipProxy, usuario, pass, requestMethod;
    private int puertoProxy;
    private final JSONParser parseador = new JSONParser();
    private EventoRedListener listener;
    private final AccionRed accion;
    private volatile boolean unaVez, repetir;
    private long tiempo, intervaloComunicacion;
    private final List<FiltroRed> filtros = Collections.synchronizedList(new ArrayList<FiltroRed>());
    private Logger log;
    private CookieManager cookiesEnviables, cookiesRecibidas;

    /**
     *
     * @param urlServidor
     * @param accion
     * @param listener
     * @param intervaloComunicacion
     */
    public Comunicador(String urlServidor, AccionRed accion, EventoRedListener listener, long intervaloComunicacion) {
        this.urlServidor = urlServidor;
        this.listener = listener;
        this.accion = accion;
        this.intervaloComunicacion = intervaloComunicacion;
    }

    /**
     * 
     * @param urlServidor
     * @param accion
     * @param listener 
     */
    public Comunicador(String urlServidor, AccionRed accion, EventoRedListener listener) {
        this(urlServidor, accion, listener, 0);
    }

    public String getUrlServidor() {
        return urlServidor;
    }

    public void setUrlServidor(String urlServidor) {
        this.urlServidor = urlServidor;
    }

    public String getIpProxy() {
        return ipProxy;
    }

    public void setIpProxy(String ipProxy) {
        this.ipProxy = ipProxy;
    }

    public int getPuertoProxy() {
        return puertoProxy;
    }

    public void setPuertoProxy(int puertoProxy) {
        this.puertoProxy = puertoProxy;
    }

    public EventoRedListener getListener() {
        return listener;
    }

    public void setListener(EventoRedListener listener) {
        this.listener = listener;
    }

    /**
     * Devuelve un filtro de red.
     *
     * @param nombre
     * @return
     */
    public FiltroRed getFiltro(String nombre) {
        return FiltroRed.buscar(filtros, nombre);
    }

    /**
     * Modifica o agrega un filtro de red.
     *
     * @param nombre
     * @param valor
     */
    public void setFiltro(String nombre, String valor) {
        FiltroRed filtro = FiltroRed.buscar(filtros, nombre);
        if (filtro != null) {
            filtro.setValor(valor);
        }
        else {
            filtros.add(new FiltroRed(nombre, valor));
        }
    }

    /**
     * Elimina un filtro de red.
     *
     * @param nombre
     */
    public void removeFiltro(String nombre) {
        filtros.remove(FiltroRed.buscar(filtros, nombre));
    }

    public long getIntervaloComunicacion() {
        return intervaloComunicacion;
    }

    public void setIntervaloComunicacion(long intervaloComunicacion) {
        this.intervaloComunicacion = intervaloComunicacion;
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

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public CookieManager getCookiesEnviables() {
        return cookiesEnviables;
    }

    public void setCookiesEnviables(CookieManager cookiesEnviables) {
        this.cookiesEnviables = cookiesEnviables;
    }

    public CookieManager getCookiesRecibidas() {
        return cookiesRecibidas;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Comprueba si se ha recibido un error del servidor.
     *
     * @param respuesta
     * @return El mensaje de error o null.
     */
    private String comprobarError(String respuesta) {
        String mensaje = null;

        try {
            if (respuesta != null) {
                JSONObject resultado = (JSONObject) parseador.parse(respuesta);
                if (resultado.get("error") != null && (boolean)resultado.get("error")) {
                    mensaje = (String) resultado.get("mensaje");
                }
            }
            else {
                mensaje = "Fallo de comunicación";

                System.out.println("ERROR: La respuesta estaba vacía.");
            }
        }
        catch (ClassCastException ex) {

        }
        catch (ParseException ex) {
            mensaje = "Fallo de comunicación";

            System.out.println("ERROR: Respuesta no parseable. RESPUESTA: " + respuesta);
        }

        return mensaje;
    }

    /**
     * Envía el mensaje al servidor.
     */
    private void comunicarse() {
        Map<String, String> params = accion.getParametros();

        synchronized (filtros) {
            for (FiltroRed filtro : filtros) {
                params.put(filtro.getNombre(), filtro.getValor());
            }
        }

        String respuesta = "";
        String error;
        try {
            if(log != null) {
                StringBuilder mensaje = new StringBuilder("Mensaje enviado a ");
                mensaje.append(urlServidor);
                mensaje.append(" - (");
                for(String clave : accion.getParametros().keySet()) {
                    mensaje.append(clave);
                    mensaje.append(": ");
                    mensaje.append(accion.getParametros().get(clave));
                    mensaje.append(", ");
                }
                mensaje.append(")");

                log.log(Level.INFO, mensaje.toString());
            }
            
            TransmisorHTTP transmisor = accion.getTransmisor(urlServidor, ipProxy, puertoProxy, usuario, pass);
            transmisor.setCookies(cookiesEnviables);
            transmisor.setRequestMethod(requestMethod);
            respuesta = transmisor.transmitir(params);
            cookiesRecibidas = transmisor.getCookiesRecibidas();
            error = comprobarError(respuesta);
            
            if(log != null) {
                log.log(Level.INFO, "Respuesta recibida - (" + respuesta + ")");
            }
        }
        catch (IOException e) {
            error = e.getLocalizedMessage();
            
            if(log != null) {
                log.log(Level.SEVERE, null, e);
            }
        }

        if (listener != null) {
            //Lanzamos el evento
            if (error == null) {
                listener.comunicacionCompletada(accion.parsearRespuesta(respuesta));
            }
            else {
                listener.comunicacionCompletada(new EventoRed(this, false, error));

                System.out.println(error);
            }
        }
    }

    public AccionRed getAccion() {
        return accion;
    }

    /**
     * Detiene la acción periódica.
     */
    public void detenerAccionPeriodica() {
        repetir = false;
    }

    /**
     * Ejecuta la acción con la periodicidad marcada.
     */
    public void ejecutarAccionPeriodica() {
        repetir = true;
        unaVez = false;
        iniciar();
    }

    /**
     * Ejecuta la acción una vez.
     */
    public void ejecutarAccion() {
        unaVez = true;
        iniciar();
    }

    /**
     * Crea un hilo y lo pone a comunicarse.
     *
     * @return
     */
    private Thread iniciar() {
        Thread hilo = new Thread(this, "Comunicacion");
        hilo.start();

        return hilo;
    }

    @Override
    public void run() {
        if (unaVez) {
            comunicarse();
            unaVez = false;
        }
        else {
            if (intervaloComunicacion > 0) {
                //Nos comunicamos en bucle
                while (repetir) {
                    tiempo = System.currentTimeMillis();

                    comunicarse();

                    long difference = tiempo - System.currentTimeMillis() + intervaloComunicacion;
                    try {
                        Thread.sleep(Math.max(0, difference));
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
