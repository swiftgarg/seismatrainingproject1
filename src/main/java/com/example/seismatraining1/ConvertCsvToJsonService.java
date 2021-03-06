package com.example.seismatraining1;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.kinesisanalyticsv2.model.ApplicationConfiguration;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;



public class ConvertCsvToJsonService implements RequestHandler<SQSEvent, Void> {

    private static final ApplicationContext context =
            new AnnotationConfigApplicationContext(AmazonClientS3Service.class);

   // private  Seismatraining1Application seismatraining1Application;


    @Autowired
    public ConvertCsvToJsonService() {
        context.getAutowireCapableBeanFactory().autowireBean(this);
    }

    private AmazonClientS3Service clientS3Service;
    private SqsService sqsServiceClient;

    @Autowired
    public ConvertCsvToJsonService(AmazonClientS3Service amazonClient, SqsService sqsServiceClient) {
        this.clientS3Service = amazonClient;
        this.sqsServiceClient = sqsServiceClient;
    }

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
      //  this.seismatraining1Application = new Seismatraining1Application();
        for(SQSEvent.SQSMessage msg : sqsEvent.getRecords()){
            System.out.println(new String(msg.getBody()) + "This is from Lambda Handler for csv to json");
        }
        try {
            convertToJson();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
      //  return "Lambda Handler Function Ran";
    }

   //return json file
    public void convertToJson() throws IOException {
        File input = clientS3Service.downloadFileFromS3Bucket();//have to give url later
        File output = new File("csv_testfiles/convertedFile.json");
        List<Map<?, ?>> data = readObjectsFromCsv(input);
        writeAsJson(data, output);
      //  return output;//return new File converted to JSON
    }

        public static List<Map<?, ?>> readObjectsFromCsv(File file) throws IOException {
            CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
            CsvMapper csvMapper = new CsvMapper();
            MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(bootstrap).readValues(file);
            return mappingIterator.readAll();
        }

        public static void writeAsJson(List<Map<?, ?>> data, File file) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(file, data);
        }


}
