package com.example.demo.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

public final class MultipartImageMergeUtil {

    private MultipartImageMergeUtil() {
    }

    public static MultipartFile mergeVertically(List<MultipartFile> files, String fieldName, String fileLabel) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one " + fileLabel + " is required.");
        }
        if (files.stream().anyMatch(file -> file == null || file.isEmpty())) {
            throw new IllegalArgumentException(capitalize(fileLabel) + " files must not be empty.");
        }
        if (files.size() == 1) {
            return files.getFirst();
        }

        try {
            java.util.ArrayList<BufferedImage> images = new java.util.ArrayList<>();
            int width = 0;
            int height = 0;

            for (MultipartFile file : files) {
                BufferedImage image = readImage(file);
                images.add(image);
                width = Math.max(width, image.getWidth());
                height += image.getHeight();
            }

            BufferedImage merged = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = merged.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            int currentYOffset = 0;
            for (BufferedImage image : images) {
                drawCentered(graphics, image, width, currentYOffset);
                currentYOffset += image.getHeight();
            }
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(merged, "png", outputStream);

            return new InMemoryMultipartFile(
                    fieldName,
                    buildMergedFilename(files),
                    "image/png",
                    outputStream.toByteArray());
        } catch (IOException exception) {
            throw new IllegalArgumentException(capitalize(fileLabel) + " files could not be merged.", exception);
        }
    }

    private static BufferedImage readImage(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("Unsupported image format for " + file.getOriginalFilename());
            }
            return image;
        }
    }

    private static void drawCentered(Graphics2D graphics, BufferedImage image, int canvasWidth, int yOffset) {
        int x = (canvasWidth - image.getWidth()) / 2;
        graphics.drawImage(image, x, yOffset, null);
    }

    private static String buildMergedFilename(List<MultipartFile> files) {
        String firstName = sanitize(files.getFirst().getOriginalFilename());
        return "merged-" + files.size() + "-pages-" + firstName + ".png";
    }

    private static String sanitize(String filename) {
        if (filename == null || filename.isBlank()) {
            return "page";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "File";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static final class InMemoryMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        private InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] bytes) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public byte[] getBytes() {
            return bytes.clone();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), bytes);
        }
    }
}
