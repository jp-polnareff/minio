package com.minio;


import com.domain.ResultInfo;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

@Component
public class MinioUpload {

    public static MinioClient minioClient;

    @Autowired
    private Environment env;

    public String uploadToMinio(File tempFile, String relativePath, String remoteFileName) throws InvalidEndpointException, InvalidPortException, IOException {
        //通过配置文件获取连接地址账号密码，于minio建立连接
        minioClient = new MinioClient(
                env.getProperty("minioEndpoint"),
                env.getProperty("minioAccessKey"),
                env.getProperty("minioSecretKey"));
        try {
            // 验证名称为bucketName的bucket是否存在，不存在则创建
            if (!checkBucketExists(minioClient, env.getProperty("bucket_name"))) {
                minioClient.makeBucket(env.getProperty("bucket_name"));
            }
            // 上传文件
            InputStream fileInput = new FileInputStream(tempFile);
            minioClient.putObject(env.getProperty("bucket_name"), relativePath + "/" + remoteFileName, fileInput, fileInput.available(), "application/octet-stream");
            //获取过期时间
            String property = env.getProperty("expiration_SECONDS");
            int parseInt = Integer.parseInt(property);
            int seconds = 60 * 60 * 24 * parseInt;
//			String url = minioClient.presignedGetObject(env.getProperty("bucket_name"), relativePath+"/"+remoteFileName,seconds);
            String url = null;
            //设置资源为公开，把配置文件中的openSouce设置为公开没有过期时间
            StringBuilder builder = new StringBuilder();
            builder.append("{\n");
            builder.append("    \"Statement\": [\n");
            builder.append("        {\n");
            builder.append("            \"Action\": [\n");
            builder.append("                \"s3:GetBucketLocation\",\n");
            builder.append("                \"s3:ListBucket\"\n");
            builder.append("            ],\n");
            builder.append("            \"Effect\": \"Allow\",\n");
            builder.append("            \"Principal\": \"*\",\n");
            builder.append("            \"Resource\": \"arn:aws:s3:::" + env.getProperty("bucket_name") + "\"\n");
            builder.append("        },\n");
            String openUrl = "            \"Resource\": [";
            String[] paths = env.getProperty("openSource").split(",");
            boolean isOpen = false;
            int i = 0;
            for (String string : paths) {
                if (i == paths.length - 1) {
                    openUrl += "\"arn:aws:s3:::" + env.getProperty("bucket_name") + "/" + string + "*\"]\n";
                } else {
                    openUrl += "\"arn:aws:s3:::" + env.getProperty("bucket_name") + "/" + string + "*\",";
                }

                if (relativePath.contains(string)) {
                    isOpen = true;
                    //break;
                }
                i++;
            }
            builder.append("        {\n");
            builder.append("            \"Action\": \"s3:GetObject\",\n");
            builder.append("            \"Effect\": \"Allow\",\n");
            builder.append("            \"Principal\": \"*\",\n");
            //builder.append("            \"Resource\": \"arn:aws:s3:::"+env.getProperty("bucket_name")+"/"+string+"/*\"\n");
            builder.append(openUrl);
            builder.append("        }\n");
            builder.append("    ],\n");
            builder.append("    \"Version\": \"2012-10-17\"\n");
            builder.append("}\n");
            minioClient.setBucketPolicy(env.getProperty("bucket_name"), builder.toString());
            if (isOpen) {
                //返回一个没有过期时间的地址
                url = minioClient.getObjectUrl(env.getProperty("bucket_name"), relativePath + "/" + remoteFileName);
            } else {
                //返回一个有过期事件带签证的地址
                url = minioClient.presignedGetObject(env.getProperty("bucket_name"), relativePath + "/" + remoteFileName, seconds);
            }
            if (url == null) {
                throw new IOException();
            }
            return url;
        } catch (Exception ace) {
            ace.printStackTrace();
            throw new IOException();
        }
    }

    /**
     * 验证minio上是否存在名称为bucketName的Bucket
     *
     * @param bucketName
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     * @throws InternalException
     * @throws ErrorResponseException
     * @throws NoResponseException
     * @throws InsufficientDataException
     * @throws NoSuchAlgorithmException
     * @throws InvalidBucketNameException
     * @throws InvalidKeyException
     */
    public static boolean checkBucketExists(MinioClient minioClient, String bucketName) throws InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, IOException, XmlPullParserException {
        List<Bucket> buckets = minioClient.listBuckets();
        for (Bucket bucket : buckets) {
            if (Objects.equals(bucket.name(), bucketName)) {
                return true;
            }
        }
        return false;
    }

    public ResultInfo<String> getUploadUrl(String relativePath, String remoteFileName) {

        ResultInfo<String> resultInfo = new ResultInfo<String>();
        try {
            minioClient = new MinioClient(
                    env.getProperty("minioEndpoint"),
                    env.getProperty("minioAccessKey"),
                    env.getProperty("minioSecretKey"));

            String property = env.getProperty("expiration_SECONDS");
            int parseInt = Integer.parseInt(property);
            int seconds = 60 * 60 * 24 * parseInt;
            String url = minioClient.presignedGetObject(env.getProperty("bucket_name"), "/" + relativePath + "/" + remoteFileName, seconds);
            if (url == null) {
                try {
                    throw new IOException();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            resultInfo.setCode("00");
            resultInfo.setData(url.toString());
            resultInfo.setSuccess(true);
            return resultInfo;
        } catch (Exception e) {
            resultInfo.setCode("01");
            resultInfo.setMessage(e.getMessage());
            resultInfo.setSuccess(false);
            return resultInfo;
        }

    }
}
