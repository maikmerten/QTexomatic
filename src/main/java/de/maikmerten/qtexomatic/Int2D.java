package de.maikmerten.qtexomatic;

/**
 *
 * @author maik
 */
public class Int2D {
    
    int x;
    int y;
    
    
    Int2D(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    Int2D(String xstring, String ystring) {
        this(Integer.parseInt(xstring), Integer.parseInt(ystring));
    }
    
}
