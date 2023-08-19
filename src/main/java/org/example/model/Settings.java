package org.example.model;

public class Settings {
    private Integer minTotalPrice;
    private Integer deadlineHoursFromNow;
    private Integer maxVolume;

    public Settings(Integer minTotalPrice, Integer deadlineHoursFromNow, Integer maxVolume) {
        this.minTotalPrice = minTotalPrice;
        this.deadlineHoursFromNow = deadlineHoursFromNow;
        this.maxVolume = maxVolume;
    }

    public Integer getMinTotalPrice() {
        return minTotalPrice;
    }

    public void setMinTotalPrice(Integer minTotalPrice) {
        this.minTotalPrice = minTotalPrice;
    }

    public Integer getDeadlineHoursFromNow() {
        return deadlineHoursFromNow;
    }

    public void setDeadlineHoursFromNow(Integer deadlineHoursFromNow) {
        this.deadlineHoursFromNow = deadlineHoursFromNow;
    }

    public Integer getMaxVolume() {
        return maxVolume;
    }

    public void setMaxVolume(Integer maxVolume) {
        this.maxVolume = maxVolume;
    }
}
