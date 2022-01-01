package de.maikmerten.qtexomatic;

import de.maikmerten.quaketexturetool.Wad;
import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * @author maik
 */
public class PakWriter {

    private static PakWriter instance;

    private Pak pak;
    private String pakpath;

    private PakWriter() {

    }
   
    public void writePakEntry(String pakpath, String name, byte[] data) {
        if(this.pak == null) {
            this.pak = new Pak(new File(pakpath));
            this.pakpath = pakpath;
        }

        if(!this.pakpath.equals(pakpath)) {
            flush();
            this.pak = new Pak(new File(pakpath));
            this.pakpath = pakpath;
        }
        
        this.pak.writeEntry(name, data);
    }

    public void flush() {
        if (pak == null) {
            return;
        }
        
        pak.close();
    }

    public static PakWriter getInstance() {
        if (instance == null) {
            instance = new PakWriter();
        }
        return instance;
    }

}
