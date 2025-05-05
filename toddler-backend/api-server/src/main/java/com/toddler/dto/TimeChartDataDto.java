package com.toddler.dto;

import lombok.Data;

@Data
public class TimeChartDataDto {
    private String[] labels;
    private double[] data;

    public TimeChartDataDto(String[] labels, double[] data) {
        this.labels = labels;
        this.data = data;
    }
}
