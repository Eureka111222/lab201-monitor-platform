package com.lab201.monitor.common.event;

import org.springframework.context.ApplicationEvent;

public class RagParseEvent extends ApplicationEvent {
    private Long documentId;

    public RagParseEvent(Object source, Long documentId) {
        super(source);
        this.documentId = documentId;
    }

    public Long getDocumentId() {
        return documentId;
    }
}