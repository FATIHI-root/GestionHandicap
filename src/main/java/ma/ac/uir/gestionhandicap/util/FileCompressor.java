package ma.ac.uir.gestionhandicap.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

public class FileCompressor {

    private static final float IMAGE_QUALITY = 0.75f;

    public static File compress(File source, File destDir, String baseName) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        String ext = getExtension(source.getName()).toLowerCase();

        if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")) {
            return compressImage(source, destDir, baseName);
        }
        if (ext.equals("pdf")) {
            return compressPdf(source, destDir, baseName);
        }
        File dest = new File(destDir, baseName + "." + ext);
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return dest;
    }

    private static File compressImage(File source, File destDir, String baseName) throws IOException {
        BufferedImage img = ImageIO.read(source);
        if (img == null) {
            String ext = getExtension(source.getName());
            File fallback = new File(destDir, baseName + "." + ext);
            Files.copy(source.toPath(), fallback.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return fallback;
        }

        BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();

        File dest = new File(destDir, baseName + ".jpg");
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(IMAGE_QUALITY);

        FileImageOutputStream out = new FileImageOutputStream(dest);
        try {
            writer.setOutput(out);
            writer.write(null, new IIOImage(rgb, null, null), param);
        } finally {
            writer.dispose();
            out.close();
        }
        return dest;
    }

    private static File compressPdf(File source, File destDir, String baseName) throws IOException {
        File dest = new File(destDir, baseName + ".pdf");
        PDDocument doc = Loader.loadPDF(source);
        try {
            doc.save(dest);
        } finally {
            doc.close();
        }
        return dest;
    }

    public static String getExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1);
    }

    public static String stripExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
}
