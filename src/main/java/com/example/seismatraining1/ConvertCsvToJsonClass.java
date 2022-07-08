package com.example.seismatraining1;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ConvertCsvToJsonClass {

    private AmazonClientS3Service clientS3Service;
    private SqsService sqsServiceClient;

    @Autowired
    public ConvertCsvToJsonClass(AmazonClientS3Service amazonClient, SqsService sqsServiceClient) {
        this.clientS3Service = amazonClient;
        this.sqsServiceClient = sqsServiceClient;
    }

    public File convertToJson() throws IOException {
        File input = clientS3Service.downloadFileFromS3Bucket();//have to give url later
        File output = new File("csv_testfiles/convertedFile.json");
        List<Map<?, ?>> data = readObjectsFromCsv(input);
        writeAsJson(data, output);
        return output;
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
