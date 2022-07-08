package com.example.seismatraining1;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.tests.EventLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.NotYetBoundException;

@SpringBootTest()
@ConfigurationProperties(prefix = "amazonproperties")
class Seismatraining1ApplicationTests {

	@Value("${endpointUrl}")
	private String endpointUrl;
	@Value("${bucketName}")
	private String bucketName;

	@Value("${accessKey}")
	private String accessKey;
	@Value("${secretKey}")
	private String secretKey;



	private AmazonClientS3Service amazonClient;
	private SqsService sqsServiceClient;

	@Autowired
	Seismatraining1ApplicationTests(AmazonClientS3Service amazonClient, SqsService sqsServiceClient) {
		this.amazonClient = amazonClient;
		this.sqsServiceClient = sqsServiceClient;
	}

	@Test
	void contextLoads() {


	}

	@Test
	public void testCsvToJsonLambdaHandler(){
		SQSEvent event = EventLoader.loadSQSEvent("event.json");
		ConvertCsvToJsonService csvToJsonService = new ConvertCsvToJsonService(amazonClient,sqsServiceClient);
		//Assertions.assertEquals("",csvToJsonService.handleRequest(event,null).toString());
		//csvToJsonService.handleRequest(event,null).toString();
		Assertions.assertEquals("Hello from SQS!",new String(event.getRecords().get(0).getBody()));


	}

	@Test
	void testFileUploader(){
		MultipartFile file = new MultipartFile() {
			@Override
			public String getName() {
				return "Test File 1";
			}

			@Override
			public String getOriginalFilename() {
				return "TF1";
			}

			@Override
			public String getContentType() {
				return null;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public long getSize() {
				return 33;
			}

			@Override
			public byte[] getBytes() throws IOException {
				return new byte[0];
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return null;
			}

			@Override
			public void transferTo(File dest) throws IOException, IllegalStateException {

			}
		};
		String urlOfUploadedFile = this.amazonClient.uploadFile(file);

		Assertions.assertEquals('l',urlOfUploadedFile.charAt(7));

	}

}
