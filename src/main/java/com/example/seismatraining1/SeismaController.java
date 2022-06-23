package com.example.seismatraining1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
    @RequestMapping("/storage/")
    public class SeismaController {

        private AmazonClientS3Service amazonClient;
        private SqsService sqsServiceClient;

        @Autowired
        SeismaController(AmazonClientS3Service amazonClient, SqsService sqsServiceClient) {
            this.amazonClient = amazonClient;
            this.sqsServiceClient = sqsServiceClient;
        }

        @PostMapping("/uploadFile")
        public String uploadFile(@RequestPart(value = "file") MultipartFile file) {
            String urlOfUploadedFile = this.amazonClient.uploadFile(file);
            String messageSentToSqsConfirmation = this.sqsServiceClient.sendMessageToQueue(urlOfUploadedFile);
            //Call function to get url of file from message and pass to url finder function
         //   File downloadedFile = amazonClient.downloadFileFromS3Bucket(null);
            //call function to get file from url and pass to converter csv json function

            //call function to csv converter and pass received file to uploader again
            //By calling upload here again we just upload new file to S3

            return urlOfUploadedFile + "    \n"  + messageSentToSqsConfirmation + "  \n Current Queue Contains Following Messages\n " + this.sqsServiceClient.readMessagesFromQueue();
        }

    }

