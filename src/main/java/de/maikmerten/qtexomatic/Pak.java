package de.maikmerten.qtexomatic;

import de.maikmerten.quaketexturetool.StreamOutput;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author maik
 */
public class Pak extends StreamOutput {

    private final File file;
    private final OutputStream os;
    private long dataoffset;
    private final List<PakEntry> entries = new ArrayList<>();

    public Pak(File file) {
        try {
            this.file = file;
            this.os = new FileOutputStream(file);

            byte[] dummyheader = new byte[12];
            this.os.write(dummyheader);
            this.dataoffset = dummyheader.length;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class PakEntry extends StreamOutput {

        private byte[] namebytes;
        private int datalen;
        private int dataoffset;

        private PakEntry(String name, int datalen, int dataoffset) throws Exception {
            byte[] asciibytes = name.getBytes("US-ASCII");
            if (asciibytes.length > 55) {
                throw new RuntimeException("PAK filename is too long: " + name);
            }

            namebytes = new byte[56];
            for (int i = 0; i < namebytes.length; i++) {
                if (i < asciibytes.length) {
                    namebytes[i] = asciibytes[i];
                } else {
                    namebytes[i] = 0;
                }
            }
            namebytes[namebytes.length - 1] = 0;

            this.datalen = datalen;
            this.dataoffset = dataoffset;
        }

        public void writeHeader(OutputStream os) throws Exception {
            os.write(namebytes);
            writeLittle(dataoffset, os);
            writeLittle(datalen, os);
        }
    }

    public void writeEntry(String name, byte[] data) {
        try {
            PakEntry entry = new PakEntry(name, data.length, (int) dataoffset);
            entries.add(entry);

            os.write(data);
            dataoffset += data.length;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            for (PakEntry e : entries) {
                e.writeHeader(os);
            }
            this.os.close();

            byte[] magic = "PACK".getBytes("US-ASCII");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writeLittle((int) dataoffset, baos);
            baos.close();
            byte[] tableoffset = baos.toByteArray();

            baos = new ByteArrayOutputStream();
            writeLittle(entries.size() * 64, baos);
            baos.close();
            byte[] tablesize = baos.toByteArray();

            RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
            raf.seek(0);
            raf.write(magic);
            raf.write(tableoffset);
            raf.write(tablesize);
            raf.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {

        File f = new File("/tmp/blubb.pak");
        Pak p = new Pak(f);

        byte[] entrydata = "Wheels are round for a good reason!".getBytes("US-ASCII");
        p.writeEntry("test.txt", entrydata);

        entrydata = "Das Schlitten muss gestoppen werden!".getBytes("US-ASCII");
        p.writeEntry("stop.txt", entrydata);

        p.close();

    }

}
