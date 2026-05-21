package shit.zen.utils;

import java.security.SecureRandom;
import java.util.Arrays;

public class ByteCipher {
    private final byte[] key;

    public ByteCipher(byte[] keyBytes) {
        this.key = Arrays.copyOf(keyBytes, keyBytes.length);
    }

    public byte[] encrypt(byte[] input, int shift) {
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; ++i) {
            output[i] = (byte)(input[i] + this.key[i % this.key.length] + shift);
        }
        return output;
    }

    public byte[] decrypt(byte[] input, int shift) {
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; ++i) {
            output[i] = (byte)(input[i] - this.key[i % this.key.length] - shift);
        }
        return output;
    }

    static {
        new SecureRandom();
    }
}