package com.example.seismatraining1;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

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
        public String uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException {
            // KAFKA CONFIG

            Properties producerProperties = new Properties();
            producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, " 172.17.208.1:9092");//Using WSL UPv4
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            producerProperties.put(ProducerConfig.RETRIES_CONFIG, 0);
            producerProperties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 2);

            //Creating producer
            Producer<String, String> producer = new KafkaProducer<>(producerProperties);


            String urlOfUploadedFile = this.amazonClient.uploadFile(file);

            ProducerRecord<String, String> record = new ProducerRecord<>("seismaTest", urlOfUploadedFile);// Sending url of uploaded file to kakfa

            producer.send(record);
            producer.flush();




            String messageSentToSqsConfirmation = this.sqsServiceClient.sendMessageToQueue(urlOfUploadedFile);


            //Call function to get url of file from message and pass to url finder function
         //   File downloadedFile = amazonClient.downloadFileFromS3Bucket(null);
            //call function to get file from url and pass to converter csv json function

            //call function to csv converter and pass received file to uploader again
            //By calling upload here again we just upload new file to S3

            //create kafka consumer
            Properties consumerProperties = new Properties();
            consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.17.208.1:9092");
            consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group");
            consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            Consumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
            //subscribe to topic
            consumer.subscribe(Collections.singleton("seismaTest"));


                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100000));
              /*  String recordOutput = "";
                for (ConsumerRecord<String, String> recordOfMessage : records) {
                    recordOutput = recordOfMessage.value();
                }*/
            consumer.commitSync(Duration.ofMillis(1000));



            ConvertCsvToJsonClass csvToJsonClass = new ConvertCsvToJsonClass(amazonClient,sqsServiceClient);
            File convertedFile = csvToJsonClass.convertToJson();
            String urlOfConvertedFileUploadedToS3 = this.amazonClient.uploadFileTos3bucket(convertedFile.getName(),convertedFile);







           //Sending Converted file to producer for other upload services to consume converted file

            ProducerRecord<String, String> recordConvertedFilePath = new ProducerRecord<>("seismaTest", "\nURL of converted file is : "+urlOfConvertedFileUploadedToS3);// Sending url of uploaded file to kakfa

            producer.send(recordConvertedFilePath);
            producer.flush();






            //return records.toString();





            return urlOfUploadedFile + "    \n"  + messageSentToSqsConfirmation + "  \n Current Queue Contains Following Messages\n "
                    + this.sqsServiceClient.readMessagesFromQueue() + "\n\n" + urlOfConvertedFileUploadedToS3 ;
        }

    }

