package org.wso2.carbon.bpmn.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    private static final String HEXES = "0123456789ABCDEF";

    public static byte[] createChecksum(File fileToCalculateMD5)
            throws IOException, NoSuchAlgorithmException {
        InputStream fis = new FileInputStream(fileToCalculateMD5);
        try {
            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            return complete.digest();
        } finally {
            fis.close();
        }
    }

    public static String getMD5Checksum(File file) throws IOException, NoSuchAlgorithmException {
        byte[] b = createChecksum(file);
        return getHex(b);
    }

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
