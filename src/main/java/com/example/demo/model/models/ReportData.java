package com.example.demo.model.models;

public class ReportData {
    private Integer[] label;
    private Integer[] sum;

    public ReportData(Integer[] label, Integer[] sum) {
        this.label = label;
        this.sum = sum;
    }

    public ReportData() {

    }

    public Integer[] getLabel() {
        return label;
    }

    public void setLabel(Integer[] label) {
        this.label = label;
    }

    public Integer[] getSum() {
        return sum;
    }

    public void setSum(Integer[] sum) {
        this.sum = sum;
    }
}
