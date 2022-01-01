package de.maikmerten.qtexomatic;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author maik
 */
public class Opts {
    
    private static Opts instance;
    private final Map<String, Object> opts;
    
    public final static String OFFSET = "offset";
    public final static String WADFILE = "wadfile";
    public final static String WADDITHER = "waddither";
    public final static String PAKFILE = "pakfile";
    
    private Opts() {
        this.opts = new HashMap<>();
        
        // set default options
        opts.put(OFFSET, new Int2D(0, 0));
        opts.put(WADFILE, "output.wad");
        opts.put(WADDITHER, 0.25);
        opts.put(PAKFILE, "pak0.pak");
    }
    
    public Object get(String key) {
        return opts.get(key);
    }
    
    public void put(String key, Object o) {
        opts.put(key, o);
    }
    
    
    public static Opts getInstance() {
        if(instance == null) {
            instance = new Opts();
        }
        return instance;
    }
    
}
