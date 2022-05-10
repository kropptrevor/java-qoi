package xyz.trevorkropp.qoi;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StandardEncoder implements Encoder {

    private Image image;
    private OutputStream out;

    private RGBA[] cache = new RGBA[64];

    public StandardEncoder(OutputStream out, Image image) {
        this.image = image;
        this.out = out;
    }

    public static void encode(OutputStream out, Image image) throws IOException {
        StandardEncoder encoder = new StandardEncoder(out, image);
        encoder.encode();
    }

    public void encode() throws IOException {
        writeHeader();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                writeChunk(x, y);
            }
        }
        writeEndMarker();
    }

    private void writeHeader() throws IOException {
        out.write(new byte[] { 'q', 'o', 'i', 'f' });
        out.write(intToBytes(image.getWidth()));
        out.write(intToBytes(image.getHeight()));
        out.write(4);
        out.write(0);
    }

    private void writeChunk(int x, int y) throws IOException {
        byte previousAlpha = (byte) 255;
        RGBA pixel = image.getAt(x, y);
        int index = calculateIndex(pixel);
        RGBA cachePixel = cache[index];
        if (pixel.equals(cachePixel)) {
            out.write((byte) index);
        } else if (previousAlpha == pixel.getA()) {
            out.write(0b11111110);
            out.write(pixel.getR());
            out.write(pixel.getG());
            out.write(pixel.getB());
            cache[index] = pixel;
        } else {
            out.write(0b11111111);
            out.write(pixel.getR());
            out.write(pixel.getG());
            out.write(pixel.getB());
            out.write(pixel.getA());
            cache[index] = pixel;
        }
    }

    private void writeEndMarker() throws IOException {
        out.write(new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 });
    }

    private int calculateIndex(RGBA rgba) {
        int r = rgba.getR() & 0xFF;
        int g = rgba.getG() & 0xFF;
        int b = rgba.getB() & 0xFF;
        int a = rgba.getA() & 0xFF;
        return (r * 3 + g * 5 + b * 7 + a * 11) % 64;
    }

    private byte[] intToBytes(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }

}
