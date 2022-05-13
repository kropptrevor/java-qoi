package xyz.trevorkropp.qoi;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StandardEncoder implements Encoder {

    private Image image;
    private OutputStream out;

    private RGBA[] cache;
    private RGBA prev;

    public StandardEncoder(OutputStream out, Image image) {
        this.image = image;
        this.out = out;
    }

    public static void encode(OutputStream out, Image image) throws IOException {
        StandardEncoder encoder = new StandardEncoder(out, image);
        encoder.encode();
    }

    public void encode() throws IOException {
        reset();
        writeHeader();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                writeChunk(x, y);
            }
        }
        writeEndMarker();
    }

    private void reset() {
        cache = new RGBA[64];
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new RGBA(0, 0, 0, 0);
        }
        prev = new RGBA(0, 0, 0, 255);
    }

    private void writeHeader() throws IOException {
        out.write(new byte[] { 'q', 'o', 'i', 'f' });
        out.write(intToBytes(image.getWidth()));
        out.write(intToBytes(image.getHeight()));
        out.write(4);
        out.write(0);
    }

    private int diff(byte prev, byte next) {
        int prevNum = prev & 0xFF;
        int nextNum = next & 0xFF;
        return (nextNum - prevNum + 2) % 256;
    }

    private boolean isSmallDiff(int diff) {
        return diff <= 3;
    }

    private void writeChunk(int x, int y) throws IOException {
        RGBA pixel = image.getAt(x, y);
        int index = calculateIndex(pixel);
        RGBA cachePixel = cache[index];
        if (pixel.equals(cachePixel)) {
            writeIndexChunk(index);
        } else if (prev.getA() == pixel.getA()) {
            int dr = diff(prev.getR(), pixel.getR());
            int dg = diff(prev.getG(), pixel.getG());
            int db = diff(prev.getB(), pixel.getB());
            if (isSmallDiff(dr) && isSmallDiff(dg) && isSmallDiff(db)) {
                writeDiffChunk(dr, dg, db);
            } else {
                writeRGBChunk(pixel);
            }
            cache[index] = pixel;
        } else {
            writeRGBAChunk(pixel);
            cache[index] = pixel;
        }
        prev = pixel;
    }

    private void writeRGBChunk(RGBA pixel) throws IOException {
        out.write(0b11111110);
        out.write(pixel.getR());
        out.write(pixel.getG());
        out.write(pixel.getB());
    }

    private void writeRGBAChunk(RGBA pixel) throws IOException {
        out.write(0b11111111);
        out.write(pixel.getR());
        out.write(pixel.getG());
        out.write(pixel.getB());
        out.write(pixel.getA());
    }

    private void writeIndexChunk(int index) throws IOException {
        out.write((byte) index);
    }

    private void writeDiffChunk(int dr, int dg, int db) throws IOException {
        byte diffByte = 0b01 << 6;
        diffByte |= dr << 4;
        diffByte |= dg << 2;
        diffByte |= db;
        out.write(diffByte);
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
