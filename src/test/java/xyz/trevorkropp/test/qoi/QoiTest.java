package xyz.trevorkropp.test.qoi;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import xyz.trevorkropp.qoi.Image;
import xyz.trevorkropp.qoi.QoiEncoder;
import xyz.trevorkropp.qoi.RGBA;

public class QoiTest {

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
        QoiEncoder q = new QoiEncoder(bab, image);

        q.encode();

        byte[] bytes = bab.toByteArray();
        byte[] actual = Arrays.copyOfRange(bytes, 0, 14);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void shouldHaveEndMarker() throws IOException {
        byte[] expected = new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 };
        Image image = new Image(100, 200);
        ByteArrayOutputStream bab = new ByteArrayOutputStream();
        QoiEncoder q = new QoiEncoder(bab, image);

        q.encode();

        byte[] bytes = bab.toByteArray();
        byte[] actual = Arrays.copyOfRange(bytes, bytes.length - 8, bytes.length);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void shouldHaveRGBAChunk() throws IOException {
        ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream(14);
        expectedOutput.write(0b11111111); // tag
        expectedOutput.write(0); // r
        expectedOutput.write(128); // g
        expectedOutput.write(0); // b
        expectedOutput.write(255); // a
        byte[] expected = expectedOutput.toByteArray();
        Image image = new Image(100, 200);
        image.setAt(0, 0, new RGBA(0, 128, 0, 255));
        ByteArrayOutputStream bab = new ByteArrayOutputStream();
        QoiEncoder q = new QoiEncoder(bab, image);

        q.encode();

        byte[] bytes = bab.toByteArray();
        byte[] actual = Arrays.copyOfRange(bytes, 14, 19);
        assertArrayEquals(expected, actual);
    }

    private byte[] intToBytes(final int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }
}
