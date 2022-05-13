package xyz.trevorkropp.test.qoi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import xyz.trevorkropp.qoi.Image;
import xyz.trevorkropp.qoi.RGBA;

public final class Util {

    private Util() {
    }

    private static Image convertAWTBufferedImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        Image image = new Image(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = bufferedImage.getRGB(x, y);
                int alpha = rgba >>> 24 & 0xFF;
                int red = rgba >>> 16 & 0xFF;
                int green = rgba >>> 8 & 0xFF;
                int blue = rgba & 0xFF;
                image.setAt(x, y, new RGBA(red, green, blue, alpha));
            }
        }
        return image;
    }

    public static Image readToImage(Path path) throws IOException {
        BufferedImage pngAWTImage = ImageIO.read(path.toFile());
        return convertAWTBufferedImage(pngAWTImage);
    }
}
