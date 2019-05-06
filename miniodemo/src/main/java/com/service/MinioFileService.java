package com.service;


import com.domain.ResultInfo;
import com.minio.MinioUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
public class MinioFileService {

    @Autowired
    MinioUpload minioUpload;

    @Autowired
    private Environment env;

    public ResultInfo<String> savefile(MultipartFile file) {
        File fileToSave = null;
        ResultInfo<String> resultInfo = new ResultInfo<String>();
        try {
            fileToSave = new File(file.getOriginalFilename());
            byte[] bytes = file.getBytes();
            FileCopyUtils.copy(bytes, fileToSave);
            String remoteFileName = UUID.randomUUID() + file.getOriginalFilename();
            String path = minioUpload.uploadToMinio(fileToSave, "video", remoteFileName);
            String data = "video" + "," + remoteFileName;
            resultInfo.setData(data);
            resultInfo.setSuccess(true);
            resultInfo.setMessage(path);
        } catch (Exception e) {
            // TODO: handle exception
            resultInfo.setSuccess(false);
            resultInfo.setMessage(e.getMessage());
        } finally {
            if (fileToSave != null) {
                fileToSave.delete();
            }
        }
        return resultInfo;
    }

    public ResultInfo<String> getUploadUrl(String relativePath, String remoteFileName) {

        ResultInfo<String> uploadUrl = minioUpload.getUploadUrl(relativePath, remoteFileName);
        return uploadUrl;
    }
}
