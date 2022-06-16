package com.example.seismatraining1;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@ConfigurationProperties(prefix = "amazonproperties")
public class SqsService {


    private AmazonSQS sqsClient;
    private String queue_url;

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
        this.sqsClient =  AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl,"us-west-2"))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        CreateQueueRequest createStandardQueueRequest = new CreateQueueRequest("seismaMessageSqsQueue3");
        queue_url = sqsClient.createQueue(createStandardQueueRequest).getQueueUrl();

    }

    public String sendMessageToQueue(String fileURL){
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queue_url)
                .withMessageBody(fileURL)//sending the url of newly created file in s3 bucket to sqs message for next ms
                .withDelaySeconds(5);
        sqsClient.sendMessage(send_msg_request);
        return "Message sent to SQS Queue at URL :" + queue_url;

    }
    public String readMessagesFromQueue(){
        List<Message> messages = sqsClient.receiveMessage(queue_url).getMessages();

        return messages.toString();
    }

    public String findFileURLFromMessage(){
        String urlFound = "";

        //Connect to sns and receive message, return file url from that

        return  urlFound;
    }

}
