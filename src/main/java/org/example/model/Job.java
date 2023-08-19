package org.example.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import static org.example.Config.CURRENT_YEAR;

public class Job {
    private String title;
    private LocalDateTime dueDate;
    private String price;
    private String wordsCount;
    private String customer;
    private LocalDateTime appeared;


    public LocalDateTime getAppeared() {
        return appeared;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDateStr) {
        String inputWithYear = CURRENT_YEAR + dueDateStr;

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy EEEE, MMMM d, h:mm a zzz")
                .toFormatter();

        TemporalAccessor temporalAccessor = formatter.parse(inputWithYear);
        this.dueDate =  LocalDateTime.from(temporalAccessor);
    }

    public void setAppeared(LocalDateTime appeared) {
        this.appeared = appeared;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getWordsCount() {
        return wordsCount;
    }

    public void setWordsCount(String wordsCount) {
        this.wordsCount = wordsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return title.equals(job.title) && dueDate.equals(job.dueDate) && price.equals(job.price) && customer.equals(job.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, dueDate, price, customer);
    }

    @Override
    public String toString() {
        return "--- Job: " +
                "Price:" + price + '\'' +
                "Customer:'" + customer + '\'' +
                "Title:" + title + '\'' +
                "DueDate:" + dueDate + '\'' +
                " ---";
    }
}
