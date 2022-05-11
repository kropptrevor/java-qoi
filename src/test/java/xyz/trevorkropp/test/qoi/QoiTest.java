package xyz.trevorkropp.test.qoi;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import xyz.trevorkropp.qoi.Image;
import xyz.trevorkropp.qoi.RGBA;
import xyz.trevorkropp.qoi.StandardEncoder;

public class QoiTest {

    private byte[] intToBytes(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }

    @Test
    public void shouldHaveHeader() throws IOException {
        ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream(14);
        expectedOutput.write(new byte[] { 'q', 'o', 'i', 'f' }); // magic
        expectedOutput.write(intToBytes(100)); // width
        expectedOutput.write(intToBytes(200)); // height
        expectedOutput.write(4); // channels
        expectedOutput.write(0); // colorspace
        byte[] expected = expectedOutput.toByteArray();
        Image image = new Image(100, 200);
        ByteArrayOutputStream bab = new ByteArrayOutputStream();

        StandardEncoder.encode(bab, image);

        byte[] bytes = bab.toByteArray();
        byte[] actual = Arrays.copyOfRange(bytes, 0, 14);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void shouldHaveEndMarker() throws IOException {
        byte[] expected = new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 };
        Image image = new Image(100, 200);
        ByteArrayOutputStream bab = new ByteArrayOutputStream();

        StandardEncoder.encode(bab, image);

        byte[] bytes = bab.toByteArray();
        byte[] actual = Arrays.copyOfRange(bytes, bytes.length - 8, bytes.length);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void shouldHaveRGBAChunk() throws IOException {
        ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream(14);
        expectedOutput.write(0b11111111); // tag
        expectedOutput.write(0); // r
        expectedOutput.write(0); // g
        expectedOutput.write(0); // b
        expectedOutput.write(128); // a
        byte[] expected = expectedOutput.toByteArray();
        Image image = new Image(100, 200);
        image.setAt(0, 0, new RGBA(0, 0, 0, 128));
        ByteArrayOutputStream bab = new ByteArrayOutputStream();

        StandardEncoder.encode(bab, image);

        byte[] bytes = bab.toByteArray();
        byte[] actual = Arrays.copyOfRange(bytes, 14, 19);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void shouldHaveRGBChunk() throws IOException {
        ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream(14);
        expectedOutput.write(0b11111110); // tag
        expectedOutput.write(128); // r
        expectedOutput.write(0); // g
        expectedOutput.write(0); // b
        byte[] expected = expectedOutput.toByteArray();
        Image image = new Image(100, 200);
        image.setAt(0, 0, new RGBA(128, 0, 0, 255));
        ByteArrayOutputStream bab = new ByteArrayOutputStream();

        StandardEncoder.encode(bab, image);

        byte[] bytes = bab.toByteArray();
        byte[] actual = Arrays.copyOfRange(bytes, 14, 18);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void shouldHaveIndexChunk() throws IOException {
        byte expected = (byte) 53;
        Image image = new Image(100, 200);
        image.setAt(0, 0, new RGBA(128, 0, 0, 255));
        image.setAt(1, 0, new RGBA(0, 127, 0, 255));
        image.setAt(2, 0, new RGBA(128, 0, 0, 255));
        ByteArrayOutputStream bab = new ByteArrayOutputStream();

        StandardEncoder.encode(bab, image);

        byte[] bytes = bab.toByteArray();
        byte actual = bytes[22];
        assertEquals(expected, actual);
    }

    @Test
    public void shouldHaveDiffChunk() throws IOException {
        byte expected = (byte) 0b01_11_10_10;
        Image image = new Image(100, 200);
        image.setAt(0, 0, new RGBA(128, 0, 0, 255));
        image.setAt(1, 0, new RGBA(129, 0, 0, 255));
        ByteArrayOutputStream bab = new ByteArrayOutputStream();

        StandardEncoder.encode(bab, image);

        byte[] bytes = bab.toByteArray();
        byte actual = bytes[18];
        assertEquals(expected, actual);
    }

    @Test
    public void shouldEncodeConsistently() throws IOException {
        Image image = new Image(100, 200);
        image.setAt(0, 0, new RGBA(130, 0, 0, 255));
        image.setAt(0, 1, new RGBA(130, 0, 0, 255));
        ByteArrayOutputStream bab = new ByteArrayOutputStream();
        StandardEncoder encoder = new StandardEncoder(bab, image);
        encoder.encode();
        byte[] expected = bab.toByteArray();
        bab.reset();

        encoder.encode();

        byte[] actual = bab.toByteArray();
        assertArrayEquals(expected, actual);
    }

}
