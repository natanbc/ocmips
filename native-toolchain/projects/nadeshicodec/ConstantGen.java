import java.nio.charset.StandardCharsets;

public class ConstantGen {
    public static void main(String[] args) {
        //pre-generate constants from https://git.asie.pl/asie-minecraft/Nadeshicodec/src/branch/master/lua/rin.lua#L38-L72
        byte[][] quad = new byte[256][];
        quad[0] = " ".getBytes(StandardCharsets.UTF_8);
        for (int i = 1; i <= 255; i++) {
            int dat = (i & 0x01) << 7;
            dat = dat | (i & 0x02) >> 1 << 6;
            dat = dat | (i & 0x04) >> 2 << 5;
            dat = dat | (i & 0x08) >> 3 << 2;
            dat = dat | (i & 0x10) >> 4 << 4;
            dat = dat | (i & 0x20) >> 5 << 1;
            dat = dat | (i & 0x40) >> 6 << 3;
            dat = dat | (i & 0x80) >> 7;
            quad[i] = String.valueOf((char)(0x2800 | dat)).getBytes(StandardCharsets.UTF_8);
        }
        int max = 0;
        for (byte[] bytes : quad) {
            max = Math.max(max, bytes.length);
        }
        System.out.println("#pragma once");
        System.out.println("#define QUAD_LEN_PER_CHAR " + max);
        System.out.println("#define QUAD(i) (&QUADS[(QUAD_LEN_PER_CHAR+1)*(i)])");
        System.out.print("const uint8_t QUADS[] = {");
        for(int i = 0; i < quad.length; i++) {
            if(i != 0) {
                System.out.println(",");
            } else {
                System.out.println();
            }
            byte[] q = quad[i];
            System.out.print("  ");
            for(int j = 0; j < q.length; j++) {
                if(j != 0) System.out.print(", ");
                System.out.print("0x" + Integer.toHexString(q[j] & 0xFF));
            }
            for(int j = q.length; j < max + 1 /* null terminator */; j++) {
                System.out.print(", 0x00");
            }
        }
        System.out.println("\n};");
        for(int i = 0; i < 2; i++) {
            byte[] q = quad[i * 255];
            System.out.println("const uint8_t STR" + i + "[] = {");
            for(int j = 0; j < 160; j++) {
                if(j != 0) System.out.println(",");
                System.out.print("  ");
                for(int k = 0; k < q.length; k++) {
                    if(k != 0) System.out.print(", ");
                    System.out.print("0x" + Integer.toHexString(q[k] & 0xFF));
                }
            }
            System.out.print(", 0x00");
            System.out.println("\n};");
            System.out.println("#define STR" + i + "_AT(i) (&STR" + i + "[(159-(i))*" + q.length + "])");
        }
        System.out.println("#define STR(i,chr) ((chr)==1?STR1_AT(i):STR0_AT(i))");
        System.out.print("const int PALETTE[] = {\n  ");
        for(int i = 0; i < 256; i++) {
            int val;
            if(i < 16) {
                val = (i * 15) << 16 | (i * 15) << 8 | (i * 15);
            } else {
                int j = i - 16;
                int b = (int)Math.floor((j % 5) * 255 / 4.0);
                int g = (int)Math.floor((Math.floor(j / 5.0) % 8) * 255 / 7.0);
                int r = (int)Math.floor((Math.floor(j / 40.0) % 6) * 255 / 5.0);
                val = r << 16 | g << 8 | b;
            }
            if(i != 0) {
                System.out.print(i % 8 == 0 ? ",\n  " : ", ");
            }
            System.out.format("0x%06x", val);
        }
        System.out.println("\n};");
    }
}
