package com.example.demo.model.models;

public class ReportComponent {
    private Integer[] ChartLabel;
    private Integer[] application;
    private  Integer[] authorised;
    private Integer[] payment;

    public ReportComponent(Integer[] chartLabel, Integer[] application, Integer[] authorised, Integer[] payment) {
        ChartLabel = chartLabel;
        this.application = application;
        this.authorised = authorised;
        this.payment = payment;
    }

    public ReportComponent() {

    }

    public Integer[] getChartLabel() {
        return ChartLabel;
    }

    public void setChartLabel(Integer[] chartLabel) {
        ChartLabel = chartLabel;
    }

    public Integer[] getApplication() {
        return application;
    }

    public void setApplication(Integer[] application) {
        this.application = application;
    }

    public Integer[] getAuthorised() {
        return authorised;
    }

    public void setAuthorised(Integer[] authorised) {
        this.authorised = authorised;
    }

    public Integer[] getPayment() {
        return payment;
    }

    public void setPayment(Integer[] payment) {
        this.payment = payment;
    }
}
