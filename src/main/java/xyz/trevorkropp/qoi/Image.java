package xyz.trevorkropp.qoi;

import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

public class Image {

    @Getter
    @Setter
    private int width;
    @Getter
    @Setter
    private int height;

    @Getter
    @Setter
    private boolean alpha;

    private RGBA[] data;

    public Image(int width, int height) {
        this.width = width;
        this.height = height;
        int size = width * height;
        data = new RGBA[size];
        Arrays.fill(data, new RGBA(0, 0, 0, 255));
    }

    public void setAt(int x, int y, RGBA rgba) {
        int index = getIndex(x, y);
        data[index] = rgba;
        if (rgba.getA() != (byte) 255) {
            setAlpha(true);
        }
    }

    public RGBA getAt(int x, int y) {
        int index = getIndex(x, y);
        return data[index];
    }

    private int getIndex(int x, int y) {
        return x + y * width;
    }

}
