package com.example.demo.system.parsitence.models.Schedule;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class Schedule implements Serializable {
    @Id
    @Column(unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String topic;
    private String accountNumber;
    private LocalDateTime scheduleTime;

    public Schedule() {
    }

    public Schedule(Long id) {
        this.id = id;
    }

    public Schedule(Long id, String topic, String accountNumber, LocalDateTime scheduleTime) {
        this.id = id;
        this.topic = topic;
        this.accountNumber = accountNumber;
        this.scheduleTime = scheduleTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public LocalDateTime getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(LocalDateTime scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", scheduleTime=" + scheduleTime +
                '}';
    }
}
