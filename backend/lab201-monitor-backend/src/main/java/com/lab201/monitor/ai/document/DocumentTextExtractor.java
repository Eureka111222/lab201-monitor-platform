package com.lab201.monitor.ai.document;

import java.io.InputStream;

public interface DocumentTextExtractor {

    boolean supports(String fileExt, String contentType);

    ExtractResult extract(InputStream inputStream, String fileName);
}
