package com.example.test.controller;

import com.example.test.MimeType;
import com.example.test.model.File;
import com.example.test.service.FileService;
import com.example.test.service.FileServiceImpl;
import com.example.test.service.HashServiceImpl;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class MainController {

    FileService fileService = new FileServiceImpl(new HashServiceImpl());

    @ResponseStatus(OK)
    @RequestMapping(value = "/{fileId}", method = GET)
    public ResponseEntity<byte[]> getById(@PathVariable("fileId") String fileId) throws IOException {

        File fileInfo = fileService.getFileInfoById(fileId);
        byte[] fileContents = fileService.getFileContentsById(fileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(fileContents.length);
        headers.setContentType(MediaType.parseMediaType("application/x-msdownload"));
        headers.add("Content-disposition", "attachment; filename="+ fileInfo.getName());

        return new ResponseEntity<>(fileContents, headers, OK);
    }

    @RequestMapping(value = "/{fileId}", method = DELETE)
    @ResponseStatus(OK)
    public void deleteById(@PathVariable("fileId") String fileId) throws IOException {
        fileService.deleteFile(fileId);
    }

    @Bean
    public FilterRegistrationBean registration(HiddenHttpMethodFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }

    @RequestMapping(value = "/", method = POST)
    @ResponseStatus(CREATED)
    public void putFile(HttpServletRequest requestEntity) throws IOException {
        fileService.putFile(IOUtils.toByteArray(requestEntity.getInputStream()),
                requestEntity.getHeader("FileName")
        );
    }

    @ResponseStatus(OK)
    @RequestMapping(value = "/search", method = GET, params = "filename", produces = MimeType.APPLICATION_JSON)
    public Map<String, String> search(@RequestParam("filename") String filename) {
        return fileService.search(filename);
    }
}
