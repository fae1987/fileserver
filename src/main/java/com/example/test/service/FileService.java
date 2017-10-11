package com.example.test.service;

import com.example.test.model.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface FileService {
    File getFileInfoById(String fileId) throws FileNotFoundException;

    byte[] getFileContentsById(String fileId) throws IOException;

    void putFile(byte[] file, String filename) throws IOException;

    Map<String, String> search(String filename);

    void deleteFile(String fileId) throws IOException;

    boolean existsFile(String fileId);

    Map<String, File> getFiles();

    Path getFilePath();

    Path getFilePath(String filename);

    Path getCataloguePath();
}
