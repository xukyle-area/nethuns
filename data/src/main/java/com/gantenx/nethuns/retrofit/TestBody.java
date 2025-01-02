package com.gantenx.nethuns.retrofit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TestBody {
    private String symbol;
    private String side;
    private String type;
}


