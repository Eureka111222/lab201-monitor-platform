package com.lab201.monitor.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lab201.ai")
public class Lab201AiProperties {

    private final Rerank rerank = new Rerank();
    private final DocumentParse documentParse = new DocumentParse();

    @Data
    public static class Rerank {
        /**
         * local keeps the existing in-process rerank logic.
         * dashscope calls Alibaba Cloud Model Studio qwen3-rerank and falls back to local when unavailable.
         */
        private String provider = "local";
        private boolean fallbackToLocal = true;
        private final DashScope dashscope = new DashScope();
    }

    @Data
    public static class DashScope {
        private String apiKey;
        private String endpoint = "https://dashscope.aliyuncs.com/compatible-api/v1/reranks";
        private String model = "qwen3-rerank";
        private int topN = 5;
        private int timeoutSeconds = 30;
    }

    @Data
    public static class DocumentParse {
        private int timeoutSeconds = 90;
        private int chunkSize = 400;
        private int minChunkSizeChars = 80;
        private int minChunkLengthToEmbed = 5;
        private int maxNumChunks = 20000;
        private int maxChunkChars = 1800;
        private boolean libreOfficeEnabled = true;
        private String libreOfficeCommand = "soffice";
        private boolean ocrEnabled = false;
        private String tesseractCommand = "tesseract";
        private String ocrLanguage = "chi_sim+eng";
    }
}
