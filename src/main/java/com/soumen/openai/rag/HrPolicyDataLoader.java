package com.soumen.openai.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HrPolicyDataLoader {


    private final VectorStore vectorStore;

    public HrPolicyDataLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Value("classpath:/Eazybytes_HR_Policies.pdf")
    Resource hrPdf;

    @PostConstruct
    public void loadFromPdfIntoVectorStore() {
        TikaDocumentReader tdr = new TikaDocumentReader(hrPdf);
        List<Document> documents = tdr.get();
       TextSplitter tp =  TokenTextSplitter.builder().withChunkSize(100).withMaxNumChunks(400).build();
        vectorStore.add(tp.split(documents));
    }



}
