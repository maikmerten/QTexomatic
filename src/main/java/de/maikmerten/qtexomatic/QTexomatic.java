package de.maikmerten.qtexomatic;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

/**
 *
 * @author maik
 */
public class QTexomatic {

    private final static String TEXBASE = "base";
    private final static String TEXGLOW = "glow";

    private final File scriptfile;
    private final ImageMap imgs = new ImageMap();

    public QTexomatic(File scriptfile) {
        this.scriptfile = scriptfile;
    }

    private String[] tokenizeLine(String line) {
        String[] result;

        line = line.trim();
        while (true) {
            int len = line.length();
            line = line.replaceAll("  ", " ");
            if (line.length() == len) {
                break;
            }
        }

        result = line.split(" ");
        for (int i = 0; i < result.length; i++) {
            String token = result[i];
            token = token.trim();
            if (i == 0) {
                token = token.toLowerCase(Locale.ENGLISH);
            }
            result[i] = token;
        }

        return result;
    }

    private void handleSet(String[] tokens) {
        String opt = tokens[1];

        Opts opts = Opts.getInstance();

        switch (opt) {
            case Opts.OFFSET: {
                Int2D offset = new Int2D(tokens[2], tokens[3]);
                opts.put(Opts.OFFSET, offset);
                break;
            }
            
            case Opts.PAKFILE: {
                opts.put(Opts.PAKFILE, tokens[2]);
                break;
            }
            
            case Opts.WADDITHER: {
                double ditherstrength = Double.parseDouble(tokens[2]);
                opts.put(Opts.WADDITHER, ditherstrength);
                break;
            }
            
            case Opts.WADFILE: {
                opts.put(Opts.WADFILE, tokens[2]);
                break;
            }
        }
    }

    private void runLine(File baseDir, String line, int linenum) throws Exception {
        String[] tokens = tokenizeLine(line);
        if (tokens.length < 1) {
            return;
        }

        String cmd = tokens[0];
        if (cmd.startsWith("#") || cmd.length() < 1) {
            return;
        }
        
        System.out.print(linenum + ": ");
        for(String t : tokens) {
            System.out.print(t + " ");
        }
        System.out.println();    

        switch (cmd) {

            case "add": {
                String dest = tokens[1];
                BufferedImage img1 = imgs.get(tokens[2]);
                BufferedImage img2 = imgs.get(tokens[3]);
                BufferedImage result = Cmds.add(img1, img2);
                imgs.put(dest, result);
                break;
            }

            case "clearall": {
                imgs.clear();
                break;
            }

            case "combine": {
                String dest = tokens[1];
                BufferedImage img1 = imgs.get(tokens[2]);
                BufferedImage img2 = imgs.get(tokens[3]);
                BufferedImage result = Cmds.combine(img1, img2);
                imgs.put(dest, result);
                break;
            }
            
            case "copy": {
                String dest = tokens[1];
                String src = tokens[2];
                imgs.put(dest, imgs.get(src));
                break;
            }
            
            case "crop": {
                String dest = tokens[1];
                String src = tokens[2];
                int x = Integer.parseInt(tokens[3]);
                int y = Integer.parseInt(tokens[4]);
                int width = Integer.parseInt(tokens[5]);
                int height = Integer.parseInt(tokens[6]);
                BufferedImage img = imgs.get(src);
                BufferedImage result = Cmds.crop(img, x, y, width, height);
                imgs.put(dest, result);
                break;
            }

            case "yuvroundtrip": {
                String dest = tokens[1];
                BufferedImage img = imgs.get(tokens[2]);
                BufferedImage result = Cmds.yuvroundtrip(img);
                imgs.put(dest, result);
                break;
            }

            case "new": {
                String dest = tokens[1];
                Int2D dimensions = new Int2D(tokens[2], tokens[3]);
                String hexcolor = tokens[4];
                BufferedImage result = Cmds.newimg(dimensions.x, dimensions.y, hexcolor);
                imgs.put(dest, result);
                break;
            }

            case "replacecolor": {
                String dest = tokens[1];
                BufferedImage img = imgs.get(tokens[2]);
                BufferedImage result = Cmds.replacecolor(img, tokens[3], tokens[4]);
                imgs.put(dest, result);
                break;
            }

            case "replaceothercolors": {
                String dest = tokens[1];
                BufferedImage img = imgs.get(tokens[2]);
                BufferedImage result = Cmds.replaceothercolors(img, tokens[3], tokens[4]);
                imgs.put(dest, result);
                break;
            }

            case "scale": {
                String dest = tokens[1];
                BufferedImage img = imgs.get(tokens[2]);
                Int2D size = new Int2D(tokens[3], tokens[4]);
                BufferedImage result = Cmds.scale(img, size.x, size.y);
                imgs.put(dest, result);
                break;
            }

            case "scaleluma": {
                String dest = tokens[1];
                BufferedImage img1 = imgs.get(tokens[2]);
                BufferedImage img2 = imgs.get(tokens[3]);
                BufferedImage result = Cmds.scaleluma(img1, img2);
                imgs.put(dest, result);
                break;
            }

            case "set": {
                handleSet(tokens);
                break;
            }

            case "sub": {
                String dest = tokens[1];
                BufferedImage img1 = imgs.get(tokens[2]);
                BufferedImage img2 = imgs.get(tokens[3]);
                BufferedImage result = Cmds.sub(img1, img2);
                imgs.put(dest, result);
                break;
            }

            case "tile": {
                String dest = tokens[1];
                BufferedImage img = imgs.get(tokens[2]);
                Int2D repeats = new Int2D(tokens[3], tokens[4]);
                BufferedImage result = Cmds.tile(img, repeats.x, repeats.y);
                imgs.put(dest, result);
                break;
            }

            case "load": {
                String dest = tokens[1];
                String file = tokens[2];
                BufferedImage img = Cmds.load(baseDir, file);
                imgs.put(dest, img);
                break;
            }

            case "save": {
                String name = tokens[1];
                String format = tokens[2];
                BufferedImage base = imgs.get(TEXBASE);
                BufferedImage glow = null;
                if(imgs.containsKey(TEXGLOW)) {
                    glow = imgs.get(TEXGLOW);
                }
                Cmds.save(base, glow, baseDir, name, format);
                break;
            }
            default: {
                throw new RuntimeException("unknown command '" + cmd + "' in line " + linenum);
            }
        }

    }

    private void runScript() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(this.scriptfile));
        
        String parentPath = this.scriptfile.getAbsolutePath();
        parentPath = parentPath.substring(0, parentPath.lastIndexOf(File.separator) + 1);
        File baseDir = new File(parentPath);
        
        if (!baseDir.isDirectory()) {
            throw new RuntimeException("baseDir is not a directory?!");
        }
        
        String line;
        int linenum = 0;
        while ((line = br.readLine()) != null) {
            linenum++;
            try {
                runLine(baseDir, line, linenum);
            } catch(Exception e) {
                System.out.println("Error executing line " + linenum + ": " + e.getMessage());
                return;
            }
        }

    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("need scriptfile input");
        }

        File scriptfile = new File(args[0]);
        if (!scriptfile.exists()) {
            throw new IllegalArgumentException("scriptfile does not exist");
        }

        QTexomatic tg = new QTexomatic(scriptfile);
        tg.runScript();

        WadWriter.getInstance().flush();
        PakWriter.getInstance().flush();

    }

}
