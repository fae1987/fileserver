package com.example.test.service;

import com.example.test.model.File;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${files.dir.path}")
    private String FILES_DIR_PATH;

    @Value("${initfile}")
    private String CATALOGUE_PATH;

    private HashService hashService;

    private Map<String, File> files;

    public FileServiceImpl(HashService hashService){
        this.hashService = hashService;
        files = new HashMap<>();
    }

    public FileServiceImpl(String filePath, String cataloguePath, HashService hashService){
        FILES_DIR_PATH = filePath;
        CATALOGUE_PATH = cataloguePath;
        this.hashService = hashService;
        files = new HashMap<>();
    }

    @Override
    public File getFileInfoById(String fileId) throws FileNotFoundException {
        if (existsFile(fileId)){
            return files.get(fileId);
        }
        else {
            throw new FileNotFoundException(String.format("File with id = %s not found", fileId));
        }
    }

    @Override
    public byte[] getFileContentsById(String fileId) throws IOException {
        if (existsFile(fileId)){
            Path filePath = getFilePath(getFilenameById(fileId));
            return Files.readAllBytes(filePath);
        }
        else {
            throw new FileNotFoundException(String.format("File with id = %s not found", fileId));
        }
    }

    @Override
    public void putFile(byte[] file, String filename) throws IOException {
        String fileId = String.valueOf(hashService.getHash(file));
        if (!existsFile(fileId)){
            addFile(file, filename, fileId);
        }
    }

    @Override
    public Map<String, String> search(String filename) {
       if (filename == null || filename.isEmpty())
           return new HashMap<>();
       return files.values().stream()
                .filter(file -> file.getName().matches(filename.replace("*",".*?")))
                .limit(25)
                .collect(Collectors.toMap(File::getId, File::getName));
    }

    @Override
    public void deleteFile(String fileId) throws IOException {
        if (existsFile(fileId)){
            deleteFileFromFS(fileId);
            files.remove(fileId);
        }
        else{
            throw new FileNotFoundException(String.format("File with id = %s not found", fileId));
        }
    }

    @Override
    public boolean existsFile(String fileId) {
        return files.containsKey(fileId);
    }

    @Override
    public Map<String, File> getFiles() {
        return files;
    }

    @Override
    public Path getFilePath() {
        return Paths.get(FILES_DIR_PATH);
    }

    @Override
    public Path getFilePath(String filename) {
        return Paths.get(FILES_DIR_PATH + filename);
    }

    @Override
    public Path getCataloguePath() {
        return Paths.get(CATALOGUE_PATH);
    }

    @PostConstruct
    public void getFilesFromCatalogue() throws IOException {
        Path filePath = getCataloguePath();
        Type itemsMapType = new TypeToken<Map<String, File>>() {}.getType();
        Map<String, File> mapFromFile = new Gson().fromJson(new String(Files.readAllBytes(filePath)), itemsMapType);
        if (mapFromFile != null)
            files.putAll(mapFromFile);
    }

    @PreDestroy
    public void saveToCatalogue() throws IOException {
        String filesStr = new Gson().toJson(files);
        Files.write(getCataloguePath(), filesStr.getBytes());
    }

    private String getFilenameById(String fileId){
        return files.get(fileId).getName();
    }

    private void addFileToMap(String filename, String fileId) {
        File newFile = File.builder()
                .id(fileId)
                .name(filename)
                .build();
        files.put(fileId, newFile);
    }

    private void addFile(byte[] file, String filename,  String fileId) throws IOException {
        addFileToMap(filename,  fileId);
        createFileOnFS(filename, file);
    }

    private void createFileOnFS(String filename, byte[] file) throws IOException {
        Path newPath = getFilePath(filename);
        final Path tmp = newPath.getParent();
        if (tmp != null) // null will be returned if the path has no parent
            Files.createDirectories(tmp);
        Files.write(newPath, file);
    }

    private void deleteFileFromFS(String fileId) throws IOException {
        Path filePath = getFilePath(getFilenameById(fileId));
        Files.delete(filePath);
    }
}
