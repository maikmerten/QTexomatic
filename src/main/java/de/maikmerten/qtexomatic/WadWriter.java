package de.maikmerten.qtexomatic;

import de.maikmerten.quaketexturetool.Wad;
import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * @author maik
 */
public class WadWriter {

    private static WadWriter instance;

    private Wad wad;
    private String wadpath;

    private WadWriter() {

    }
    
    public void writeMipTex(String wadpath, String name, byte[] data) {
        if(this.wad == null) {
            this.wad = new Wad();
            this.wadpath = wadpath;
        }

        if(!this.wadpath.equals(wadpath)) {
            flush();
            this.wad = new Wad();
            this.wadpath = wadpath;
        }
        
        try {
            this.wad.addMipTexture(name, data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    

    public void flush() {
        if (wad == null) {
            return;
        }
        
        File outfile = new File(this.wadpath);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outfile);
            wad.write(fos);
            fos.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
    }

    public static WadWriter getInstance() {
        if (instance == null) {
            instance = new WadWriter();
        }
        return instance;
    }

}
