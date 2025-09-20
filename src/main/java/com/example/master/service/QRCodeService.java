package com.example.master.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class QRCodeService {

    @Value("${fms.file.location}")
    private String storagePath;
    private static final String QR_DIR = "/qrcodes/";
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;

    public String generate(String content) {
        try {
            BufferedImage qrImage = createQRImage(content);
            String filename = "QR_" + UUID.randomUUID() + ".png";
            Path path = Paths.get(storagePath+QR_DIR + filename);
            Files.createDirectories(path.getParent());
            ImageIO.write(qrImage, "png", path.toFile());
            return path.toString();
        } catch (Exception e) {
            throw new RuntimeException("QR generation failed", e);
        }
    }

    private BufferedImage createQRImage(String content) throws WriterException {
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}

