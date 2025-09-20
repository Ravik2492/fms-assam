package com.example.master.dtobj;

import lombok.Data;

@Data
public class BatchCreateRequest {
    private String ingredientName;
    private String unit;
    private String sellerName;
    private Double quantity;
    private Long demandId;
}

