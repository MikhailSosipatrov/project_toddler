package com.toddler.dto;

import lombok.Data;

@Data
public class ChartDataDto {
    private String[] labels;
    private int[] data;

    public ChartDataDto(String[] labels, int[] data) {
        this.labels = labels;
        this.data = data;
    }
}
