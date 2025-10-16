package com.gantenx.nethuns.retrofit;

import lombok.Data;

@Data
public class OrderResponse {
    private String symbol;
    private Long orderId;
    private Integer orderListId; // 如果该字段需要存储 -1，建议使用 Integer
    private String clientOrderId;
    private Long transactTime;
}
