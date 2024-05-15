package com.dwarfeng.tmpstg.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 内容工具类。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class ContentUtil {

    public static byte[] randomContent(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (Math.random() * 256);
        }
        return bytes;
    }

    public static String md5Checksum(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private ContentUtil() {
        throw new IllegalStateException("禁止实例化");
    }
}
