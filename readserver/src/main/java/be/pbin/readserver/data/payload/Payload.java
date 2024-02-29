package be.pbin.readserver.data.payload;

import lombok.Builder;

@Builder
public record Payload (String id, String payload) {}
