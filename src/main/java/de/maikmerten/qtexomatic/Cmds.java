package de.maikmerten.qtexomatic;

import de.maikmerten.quaketexturetool.Converter;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 *
 * @author maik
 */
public class Cmds {

    private static int hexcolor2rgb(String hexcolor) {
        if (hexcolor.startsWith("0x")) {
            hexcolor = hexcolor.substring(2, hexcolor.length());
        }

        if (hexcolor.length() != 6 && hexcolor.length() != 8) {
            throw new RuntimeException("hexcolor must have 6 (RGB) or 8 (ARGB) hexadezimal digits");
        }

        int rgb = new BigInteger(hexcolor, 16).intValue();

        if (hexcolor.length() == 6) {
            rgb |= 0xFF000000;
        }

        return rgb;
    }

    private static int clamp255(int val) {
        if (val < 0) {
            return 0;
        } else if (val > 255) {
            return 255;
        }
        return val;
    }

    private static void rgb2yuv(int rgb, double[] yuv) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        yuv[0] = 0.257 * r + 0.504 * g + 0.098 * b + 16;
        yuv[1] = -0.148 * r - 0.291 * g + 0.439 * b + 128;
        yuv[2] = 0.439 * r - 0.368 * g - 0.071 * b + 128;
    }

    private static int yuv2rgb(double[] yuv) {
        double y = yuv[0] - 16;
        double u = yuv[1] - 128;
        double v = yuv[2] - 128;

        double rd = 1.164 * y + 1.596 * v;
        double gd = 1.164 * y - 0.392 * u - 0.813 * v;
        double bd = 1.164 * y + 2.017 * u;

        int r = clamp255((int) Math.round(rd));
        int g = clamp255((int) Math.round(gd));
        int b = clamp255((int) Math.round(bd));

        return r << 16 | g << 8 | b;
    }

    private static double rgb2brightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        return (0.3 * r + 0.6 * g + 0.1 * b) / 256;
    }

    static BufferedImage add(BufferedImage img1, BufferedImage img2) {
        BufferedImage result = new BufferedImage(img1.getWidth(), img1.getHeight(), img1.getType());
        Int2D offset = (Int2D) Opts.getInstance().get(Opts.OFFSET);

        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                int x2 = x - offset.x;
                int y2 = y - offset.y;

                int rgb = img1.getRGB(x, y);

                if (x2 >= 0 && y2 >= 0 && x2 < img2.getWidth() && y2 < img2.getHeight()) {
                    // save alpha channel
                    int a = rgb & 0xFF000000;

                    int rgb2 = img2.getRGB(x2, y2);

                    int r1 = (rgb >> 16) & 0xFF;
                    int g1 = (rgb >> 8) & 0xFF;
                    int b1 = rgb & 0xFF;

                    int r2 = (rgb2 >> 16) & 0xFF;
                    int g2 = (rgb2 >> 8) & 0xFF;
                    int b2 = rgb2 & 0xFF;

                    int r = clamp255(r1 + r2);
                    int g = clamp255(g1 + g2);
                    int b = clamp255(b1 + b2);

                    rgb = (r << 16) | (g << 8) | b;

                    // re-apply alpha channel
                    rgb |= a;
                }
                result.setRGB(x, y, rgb);
            }
        }
        return result;
    }

    static BufferedImage combine(BufferedImage img1, BufferedImage img2) {
        BufferedImage result = new BufferedImage(img1.getWidth(), img1.getHeight(), img1.getType());

        Int2D offset = (Int2D) Opts.getInstance().get(Opts.OFFSET);

        Graphics g = result.getGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, offset.x, offset.y, null);

        return result;
    }
    
    static BufferedImage crop(BufferedImage img, int x, int y, int width, int height) {
        return img.getSubimage(x, y, width, height);
    }

    static BufferedImage load(File basedir, String filename) throws Exception {
        if (!filename.startsWith(File.pathSeparator)) {
            filename = basedir.getAbsolutePath() + File.separator + filename;
        }

        System.out.println("loading " + filename);
        File imgFile = new File(filename);
        if (!imgFile.exists()) {
            throw new RuntimeException("image file does not exist: " + imgFile.getAbsolutePath());
        }

        return ImageIO.read(imgFile);
    }

    static BufferedImage newimg(int xsize, int ysize, String hexcolor) {

        int rgb = hexcolor2rgb(hexcolor);

        boolean hasalpha = hexcolor.replaceAll("0x", "").length() == 8;
        BufferedImage result = new BufferedImage(xsize, ysize, hasalpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    static BufferedImage replacecolor(BufferedImage img, String hexcolor1, String hexcolor2) {
        BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        int rgb1 = hexcolor2rgb(hexcolor1);
        int rgb2 = hexcolor2rgb(hexcolor2);

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int c = img.getRGB(x, y);

                if (c == rgb1) {
                    c = rgb2;
                }

                result.setRGB(x, y, c);
            }
        }

        return result;
    }

    static BufferedImage replaceothercolors(BufferedImage img, String hexcolor1, String hexcolor2) {
        BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        int rgb1 = hexcolor2rgb(hexcolor1);
        int rgb2 = hexcolor2rgb(hexcolor2);

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int c = img.getRGB(x, y);
                if (c != rgb1) {
                    c = rgb2;
                }
                result.setRGB(x, y, c);
            }
        }

        return result;
    }

    static BufferedImage scale(BufferedImage img, int xsize, int ysize) {
        double oldx = img.getWidth();
        double oldy = img.getHeight();

        double scalex = (xsize / oldx);
        double scaley = (ysize / oldy);
        AffineTransform at = new AffineTransform();
        at.scale(scalex, scaley);

        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage result = new BufferedImage(xsize, ysize, img.getType());
        result = scaleOp.filter(img, result);
        return result;
    }
    
    static BufferedImage scaleluma(BufferedImage img1, BufferedImage img2) {
        BufferedImage result = new BufferedImage(img1.getWidth(), img1.getHeight(), img1.getType());
        Int2D offset = (Int2D) Opts.getInstance().get(Opts.OFFSET);

        double[] yuv = new double[3];
        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                int x2 = x - offset.x;
                int y2 = y - offset.y;

                int rgb = img1.getRGB(x, y);

                if (x2 >= 0 && y2 >= 0 && x2 < img2.getWidth() && y2 < img2.getHeight()) {
                    // save alpha channel
                    int a = rgb & 0xFF000000;
                    rgb2yuv(rgb, yuv);

                    // compute brightness scale factor and apply
                    double bright = rgb2brightness(img2.getRGB(x2, y2)) * 2.0;
                    yuv[0] *= bright;

                    rgb = yuv2rgb(yuv);
                    // apply alpha channel
                    rgb |= a;
                }
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    static BufferedImage sub(BufferedImage img1, BufferedImage img2) {
        BufferedImage result = new BufferedImage(img1.getWidth(), img1.getHeight(), img1.getType());
        Int2D offset = (Int2D) Opts.getInstance().get(Opts.OFFSET);

        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                int x2 = x - offset.x;
                int y2 = y - offset.y;

                int rgb = img1.getRGB(x, y);

                if (x2 >= 0 && y2 >= 0 && x2 < img2.getWidth() && y2 < img2.getHeight()) {
                    // save alpha channel
                    int a = rgb & 0xFF000000;

                    int rgb2 = img2.getRGB(x2, y2);

                    int r1 = (rgb >> 16) & 0xFF;
                    int g1 = (rgb >> 8) & 0xFF;
                    int b1 = rgb & 0xFF;

                    int r2 = (rgb2 >> 16) & 0xFF;
                    int g2 = (rgb2 >> 8) & 0xFF;
                    int b2 = rgb2 & 0xFF;

                    int r = clamp255(r1 - r2);
                    int g = clamp255(g1 - g2);
                    int b = clamp255(b1 - b2);

                    rgb = (r << 16) | (g << 8) | b;

                    // re-apply alpha channel
                    rgb |= a;
                }
                result.setRGB(x, y, rgb);
            }
        }
        return result;
    }

    static BufferedImage tile(BufferedImage img, int xrepeat, int yrepeat) {
        if (xrepeat < 1 || yrepeat < 1) {
            throw new IllegalArgumentException("tile repeats must be >= 1");
        }

        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage result = new BufferedImage(width * xrepeat, height * yrepeat, img.getType());
        Graphics g = result.getGraphics();

        for (int x = 0; x < xrepeat; x++) {
            for (int y = 0; y < yrepeat; y++) {
                g.drawImage(img, x * width, y * height, null);
            }
        }

        return result;
    }

    static BufferedImage yuvroundtrip(BufferedImage img) throws Exception {
        BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        double[] yuv = new double[3];

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);

                // save alpha channel
                int a = rgb & 0xFF000000;
                rgb2yuv(rgb, yuv);
                rgb = yuv2rgb(yuv);

                // apply alpha channel
                rgb |= a;

                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    private static void saveWad(BufferedImage base, BufferedImage glow, File basedir, String name) throws Exception {
        if((base.getWidth() % 16 != 0) || (base.getHeight() % 16 != 0)) {
            throw new RuntimeException("texture dimensions must be multiples of 16 for WAD textures");
        }
        
        if(name.length() > 15) {
            throw new RuntimeException("texture names for WAD textures may not have more than 15 characters");
        }
        
        Converter conv = new Converter();

        float dither = (float) (double) Opts.getInstance().get(Opts.WADDITHER);
        conv.setDitherStrength(dither);
        conv.setReduce(1);

        byte[] miptexdata = conv.convert(base, null, glow, name, false);
        if(miptexdata == null) {
            throw new RuntimeException("could not convert texture for WAD");
        }

        String wadfile = (String) Opts.getInstance().get(Opts.WADFILE);
        if (!wadfile.toLowerCase(Locale.ENGLISH).endsWith(".wad")) {
            wadfile += ".wad";
        }
        String wadpath = basedir.getAbsolutePath() + File.separator + wadfile;

        WadWriter.getInstance().writeMipTex(wadpath, name, miptexdata);

    }

    private static void savePak(BufferedImage base, BufferedImage glow, File basedir, String name) throws Exception {
        String pakfile = (String) Opts.getInstance().get(Opts.PAKFILE);
        if (!pakfile.toLowerCase(Locale.ENGLISH).endsWith(".pak")) {
            pakfile += ".pak";
        }
        String pakpath = basedir.getAbsolutePath() + File.separator + pakfile;

        String basename = "textures/" + name + ".tga";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(base, "tga", baos);
        baos.close();
        byte[] imgdata = baos.toByteArray();
        PakWriter.getInstance().writePakEntry(pakpath, basename, imgdata);

        if (glow != null) {
            String glowname = "textures/" + name + "_glow.tga";
            baos = new ByteArrayOutputStream();
            ImageIO.write(glow, "tga", baos);
            baos.close();
            imgdata = baos.toByteArray();
            PakWriter.getInstance().writePakEntry(pakpath, glowname, imgdata);
        }
    }

    static void save(BufferedImage base, BufferedImage glow, File basedir, String name, String format) throws Exception {
        String path = basedir.getAbsolutePath();

        format = format.toLowerCase(Locale.ENGLISH);

        if (!format.equals("wad")) {
            // replace * for liquids with #
            name = name.replaceAll("\\*", "#");
        }

        if (format.equals("wad")) {
            saveWad(base, glow, basedir, name);
            return;
        } else if (format.equals("pak")) {
            savePak(base, glow, basedir, name);
            return;
        }

        String glowname = name + "_glow." + format;
        name += ("." + format);

        String filename = path + File.separator + name;
        File outfile = new File(filename);
        System.out.println("saving to " + outfile.getAbsolutePath());
        ImageIO.write(base, format, outfile);

        if (glow != null) {
            filename = path + File.separator + glowname;
            outfile = new File(filename);
            System.out.println("saving to " + outfile.getAbsolutePath());
            ImageIO.write(glow, format, outfile);
        }

    }

}
