package gov.samhsa.c2s.c2ssofapi.service.pdf;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.IOException;


public class ImageResizer {
    /**
     * Resizes an image to a absolute width and height (the image may not be
     * proportional)
     *
     * @param inputImage      original image
     * @param scaledWidth     absolute width in pixels
     * @param scaledHeight    absolute height in pixels
     * @throws IOException
     */
    public  BufferedImage resize(BufferedImage inputImage, int scaledWidth, int scaledHeight)
            throws IOException {
        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        return outputImage;
    }

    /**
     * Resizes an image by a percentage of original size (proportional).
     *
     * @param inputImage      original image
     * @param percent         a double number specifies percentage of the output image
     *                        over the input image.
     * @throws IOException
     */
    public  BufferedImage resize(BufferedImage inputImage, double percent) throws IOException {
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        return resize(inputImage, scaledWidth, scaledHeight);
    }
}
