package com.example.seismatraining1;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class FileUploadHandler implements RequestHandler<SQSEvent, String> {

    private AmazonClientS3Service clientS3Service;

    public FileUploadHandler(){
        super();
    }


    @Autowired
    public FileUploadHandler(AmazonClientS3Service amazonClient) {
        this.clientS3Service = amazonClient;

    }

    @Override
    public String handleRequest(SQSEvent input, Context context) {

        clientS3Service.uploadFileTos3bucket("convertedFileFromCsv.json",new File("csv_testfiles/convertedFile.json"));

        return "File Successfully Uploaded to S3";
    }
}
