package com.eco.emission_estimator.model;

import jakarta.persistence.*;

@Entity
@Table(name = "appliance_logs")
public class ApplianceLog {

    public static final double INDIA_GRID_EMISSION_FACTOR = 0.675;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String applianceName;
    private double wattage;
    private double hoursPerDay;
    private int daysUsed;
    private String monthYear;

    private double monthlyKwh;
    private double co2Kg;

    public ApplianceLog() {}

    @PrePersist
    @PreUpdate
    public void calculateEnergyAndEmissions() {
        double rawKwh = (this.wattage * this.hoursPerDay * this.daysUsed) / 1000.0;
        this.monthlyKwh = Math.round(rawKwh * 100.0) / 100.0;
        this.co2Kg = Math.round((this.monthlyKwh * INDIA_GRID_EMISSION_FACTOR) * 100.0) / 100.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getApplianceName() { return applianceName; }
    public void setApplianceName(String applianceName) { this.applianceName = applianceName; }

    public double getWattage() { return wattage; }
    public void setWattage(double wattage) { this.wattage = wattage; }

    public double getHoursPerDay() { return hoursPerDay; }
    public void setHoursPerDay(double hoursPerDay) { this.hoursPerDay = hoursPerDay; }

    public int getDaysUsed() { return daysUsed; }
    public void setDaysUsed(int daysUsed) { this.daysUsed = daysUsed; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public double getMonthlyKwh() { return monthlyKwh; }
    public void setMonthlyKwh(double monthlyKwh) { this.monthlyKwh = monthlyKwh; }

    public double getCo2Kg() { return co2Kg; }
    public void setCo2Kg(double co2Kg) { this.co2Kg = co2Kg; }
}