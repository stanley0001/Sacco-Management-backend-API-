package com.example.demo.model.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SearchBody {
    private String status;
    private LocalDate fromDate;
    private LocalDate toDate;

    public SearchBody(String status, LocalDate fromDate, LocalDate toDate) {
        this.status = status;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public SearchBody() {
    }

    @Override
    public String toString() {
        return "SearchBody{" +
                "status='" + status + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
