package com.example.seismatraining1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
    @RequestMapping("/storage/")
    public class SeismaController {

        private AmazonClientService amazonClient;


        @Autowired
        SeismaController(AmazonClientService amazonClient) {
            this.amazonClient = amazonClient;
        }

        @PostMapping("/uploadFile")
        public String uploadFile(@RequestPart(value = "file") MultipartFile file) {
            return this.amazonClient.uploadFile(file);
        }

    }

