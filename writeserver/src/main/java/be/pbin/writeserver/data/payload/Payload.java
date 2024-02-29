package be.pbin.writeserver.data.payload;

import lombok.Builder;

@Builder
public record Payload (String id, String payload) {}
