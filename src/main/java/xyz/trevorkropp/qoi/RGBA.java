package xyz.trevorkropp.qoi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RGBA {

    private byte r;
    private byte g;
    private byte b;
    private byte a;

    public RGBA(int r, int g, int b, int a) {
        this((byte) r, (byte) g, (byte) b, (byte) a);
    }

}
