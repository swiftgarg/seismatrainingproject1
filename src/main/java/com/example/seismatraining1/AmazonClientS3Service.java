package com.example.seismatraining1;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@ConfigurationProperties(prefix = "amazonproperties")
public class AmazonClientS3Service {
    private AmazonS3 s3client;
    @Value("${endpointUrl}")
    private String endpointUrl;
    @Value("${bucketName}")
    private String bucketName;

    @Value("${accessKey}")
    private String accessKey;
    @Value("${secretKey}")
    private String secretKey;


    @PostConstruct
    private void setCredentialsAndInitializeConnection() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard()
                 .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566/",""))
                .withPathStyleAccessEnabled(true)//AWS Work make sure//////VERY VERY IMPORATANT else cannot find localstack paths
                // .withCredentials(new AWSStaticCredentialsProvider(credentials))
                //.withRegion("us-west-2")
                .build();
    }

    //S3 bucket uploading method requires File as a parameter, but we have MultipartFile, so we need to add method which can do this
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        FileOutputStream fileOutputStream = new FileOutputStream(convertedFile);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.close();
        return convertedFile;
    }

     public String uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return endpointUrl + "/" + bucketName + "/" + fileName;
    }


    public String uploadFile(MultipartFile multipartFile) {

        String fileUrl = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = multipartFile.getOriginalFilename();
            fileUrl = uploadFileTos3bucket(fileName, file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileUrl;
    }




    public  File downloadFileFromS3Bucket(){
      GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName,"employees.csv");

      File file = new File("csv_testfiles/downloaded.csv");
      s3client.getObject(getObjectRequest,file);
    //WHEN URL IS SENT, DOWNLOAD AND RETURN FILE

        return file;
    }







}
