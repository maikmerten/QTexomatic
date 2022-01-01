package de.maikmerten.qtexomatic;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 *
 * @author maik
 */
public class ImageMap extends HashMap<String, BufferedImage> {
    
    public BufferedImage get(String imgname) {
        BufferedImage img = super.get(imgname);
        if(img == null) {
            throw new RuntimeException("No image found with symbolic name '" + imgname + "'");
        }
        return img;
    }
    
}
