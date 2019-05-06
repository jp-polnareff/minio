package com.controller;

import com.domain.ResultInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.service.MinioFileService;

import javax.ws.rs.QueryParam;

@Controller
public class FileUploadController {

    @Autowired
    private MinioFileService minioService;
    @Autowired
    private Environment env;

    // 普通文件上传
    @RequestMapping(value = "/api/upload", method = RequestMethod.POST)
    public @ResponseBody
    ResultInfo handleFileUpload(@RequestParam("file") MultipartFile file) {
        return minioService.savefile(file);
    }

    //relativePath：minio上的文件目录，remoteFileName：mino上的文件名称
    @RequestMapping(value = "/getUploadUrl", method = RequestMethod.GET)
    public @ResponseBody
    ResultInfo<String> getUploadUrl(@QueryParam("relativePath") String relativePath,
                                    @QueryParam("remoteFileName") String remoteFileName) {
        return minioService.getUploadUrl(relativePath, remoteFileName);
    }
}