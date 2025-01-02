package com.gantenx.nethuns.socket.cryptocom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CryptoEvent {

    private String code;

    private Result result;

    @Data
    public static class Result {
        private String subscription;

        private Dat[] data;
    }

    @Data
    public static class Dat {
        @JsonProperty("k")
        private String ask;
        @JsonProperty("b")
        private String bid;
        @JsonProperty("a")
        private String last;
    }
}


