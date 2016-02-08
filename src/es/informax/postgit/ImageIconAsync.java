package es.informax.postgit;

import es.informax.postgit.red.TransmisorHTTPFichero;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Carga una imagen de manera asíncrono, colocando una imagen de carga.
 * @author daniel.vazquez
 * @version 1.3
 */
public class ImageIconAsync extends ImageIcon implements Runnable {
    private final URL url;
    private final JComponent contenedor;

    /**
     * 
     * @param localizacion
     * @param descripcion
     * @param imagenCarga
     * @param contenedor 
     */
    public ImageIconAsync(URL localizacion, String descripcion, Image imagenCarga, JComponent contenedor) {
        //Creamos el icono con la imagen de carga
        super(imagenCarga, descripcion);
        
        setImageObserver(contenedor);
        url = localizacion;
        this.contenedor = contenedor;
    }

    /**
     * 
     * @param localizacion
     * @param imagenCarga
     * @param contenedor 
     */
    public ImageIconAsync(URL localizacion, Image imagenCarga, JComponent contenedor) {
        this(localizacion, null, imagenCarga, contenedor);
    }
    
    /**
     * Inicia la carga de la imagen indicada en localizacion.
     */
    public void cargarImagen() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            //Descargamos la imagen en temporales y la modificamos
            File imagenTemp = File.createTempFile("Imax_", null);
            imagenTemp.deleteOnExit();
            
            new TransmisorHTTPFichero(url.toExternalForm(), imagenTemp).transmitir(new HashMap<String, String>());
            
            setImage(new ImageIcon(imagenTemp.getAbsolutePath()).getImage());
            contenedor.validate();
            contenedor.repaint();
        }
        catch(IOException ex) {
            System.out.println(ex);
            
            setImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
            contenedor.validate();
            contenedor.repaint();
        }
    }
}
