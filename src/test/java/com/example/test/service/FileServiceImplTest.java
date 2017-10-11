package com.example.test.service;

import com.example.test.model.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class FileServiceImplTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String testFileOne = "testFile1.txt";
    private final String partTestFile = "test*";
    private final String testFileContentOne = "testFile1";
    private final String testFileTwo = "testFile2.avi";
    private final long mockHash = 1L;

    private String testFileIdOne = String.valueOf(mockHash);

    private HashService hashService = Mockito.mock(HashServiceImpl.class);
    private FileService fileService = new FileServiceImpl("testfiles/", "cataloguetest.txt", hashService);

    @Before
    public void setup() throws IOException {
        Mockito.when(hashService.getHash(testFileContentOne.getBytes())).thenReturn(1L);
    }
    @After
    public void cleanUp() throws IOException {
        Files.deleteIfExists(fileService.getCataloguePath());
        if (Files.exists(fileService.getFilePath())) {
            Files.walk(fileService.getFilePath()).map(Path::toFile).forEach(java.io.File::delete);
        }
    }

    @Test
    public void shouldFailIfNotExists(){
        boolean result = fileService.existsFile(testFileOne);
        assertFalse(result);
    }

    @Test
    public void creationShouldSuccessIfNotExists() throws IOException {
        IOException exception = null;
        try {
            fileService.putFile(testFileContentOne.getBytes(), testFileOne);
        } catch (IOException e) {
            exception = e;
        }
        assertNull(exception);
        assertEquals(fileService.getFiles().size(), 1);
        assertTrue(fileService.getFiles().containsKey(testFileIdOne));
        assertTrue(Arrays.equals(Files.readAllBytes(fileService.getFilePath(testFileOne)),testFileContentOne.getBytes()));
    }

    @Test
    public void creationShouldNotHappenIfExists(){
        fileService.getFiles().put(testFileIdOne, File.builder().name(testFileOne).id(testFileIdOne).build());
        IOException exception = null;
        try {
            fileService.putFile(testFileContentOne.getBytes(), testFileOne);
        } catch (IOException e) {
            exception = e;
        }
        assertNull(exception);
        assertEquals(fileService.getFiles().size(), 1);
    }

    @Test
    public void searchShouldReturnEmptyMapIfNotExists(){
        Map<String, String> searchResult = fileService.search(testFileTwo);
        assertEquals(searchResult.size(), 0);
    }

    @Test
    public void searchShouldReturnNonEmptyMapIfExists(){
        fileService.getFiles().put(testFileIdOne, File.builder().name(testFileOne).id(testFileIdOne).build());

        Map<String, String> searchResult = fileService.search(testFileOne);

        assertEquals(searchResult.size(), 1);
        assertTrue(searchResult.containsValue(testFileOne));
    }

    @Test
    public void searchWithWildcardShouldReturnNonEmptyMapIfExists(){
        fileService.getFiles().put(testFileIdOne, File.builder().name(testFileOne).id(testFileIdOne).build());

        Map<String, String> searchResult = fileService.search(partTestFile);

        assertEquals(searchResult.size(), 1);
        assertTrue(searchResult.containsValue(testFileOne));
    }

    @Test
    public void getShouldSucceedIfExists() throws IOException {
        createFileOnFS(fileService.getFilePath(testFileOne), testFileContentOne.getBytes());
        fileService.getFiles().put(testFileIdOne, File.builder().name(testFileOne).id(testFileIdOne).build());

        Exception exception = null;
        byte[] bytes = null;
        try {
            bytes = fileService.getFileContentsById(testFileIdOne);
        } catch (IOException e) {
            exception = e;
        }

        assertNull(exception);
        assertTrue(Arrays.equals(bytes,testFileContentOne.getBytes()));
    }

    @Test
    public void deleteShouldFailIfNotExists(){
        Exception exception = null;
        try {
            fileService.deleteFile(testFileTwo);
        } catch (Exception e) {
            exception = e;
        }
        assertTrue(exception instanceof FileNotFoundException);
    }

    @Test
    public void deleteShouldSucceedIfExists() throws IOException {
        createFileOnFS(fileService.getFilePath(testFileOne), testFileContentOne.getBytes());
        fileService.getFiles().put(testFileIdOne, File.builder().name(testFileOne).id(String.valueOf(mockHash)).build());
        Exception exception = null;
        try {
            fileService.deleteFile(testFileIdOne);
        } catch (Exception e) {
            exception = e;
        }
        assertNull(exception);
        assertEquals(fileService.getFiles().size(), 0);
        assertFalse(Files.exists(fileService.getFilePath(testFileOne)));
    }

    private void createFileOnFS(Path filePath, byte[] file) throws IOException {
        final Path tmp = filePath.getParent();
        if (tmp != null) // null will be returned if the path has no parent
            Files.createDirectories(tmp);
        Files.write(filePath, file);
    }
}