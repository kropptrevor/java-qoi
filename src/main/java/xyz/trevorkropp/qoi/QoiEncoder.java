package xyz.trevorkropp.qoi;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class QoiEncoder {

    private Image image;
    private OutputStream out;

    public QoiEncoder(OutputStream out, Image image) {
        this.image = image;
        this.out = out;
    }

    public void encode() throws IOException {
        writeHeader();
        writeChunk();
        writeEndMarker();
    }

    private void writeHeader() throws IOException {
        out.write(new byte[] { 'q', 'o', 'i', 'f' });
        out.write(intToBytes(image.getWidth()));
        out.write(intToBytes(image.getHeight()));
        out.write(4);
        out.write(0);
    }

    private void writeChunk() throws IOException {
        byte previousAlpha = (byte) 255;
        RGBA pixel = image.getAt(0, 0);
        if (previousAlpha == pixel.getA()) {
            out.write(0b11111110);
            out.write(pixel.getR());
            out.write(pixel.getG());
            out.write(pixel.getB());
        } else {
            out.write(0b11111111);
            out.write(pixel.getR());
            out.write(pixel.getG());
            out.write(pixel.getB());
            out.write(pixel.getA());
        }
    }

    private void writeEndMarker() throws IOException {
        out.write(new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 });
    }

    private byte[] intToBytes(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }

}
