package com.example.test.service;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class HashServiceImpl implements HashService {
    @Override
    public long getHash(byte[] content) {
        Checksum checksum = new CRC32();
        checksum.update(content, 0, content.length);
        return checksum.getValue();
    }
}
