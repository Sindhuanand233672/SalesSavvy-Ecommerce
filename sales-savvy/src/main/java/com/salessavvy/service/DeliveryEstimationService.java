
package com.salessavvy.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DeliveryEstimationService {

    private final int cutoffHour;
    private final int transitBusinessDays;

    public DeliveryEstimationService(
            @Value("${app.delivery.cutoff-hour:14}") int cutoffHour,
            @Value("${app.delivery.transit-business-days:2}") int transitBusinessDays) {

        this.cutoffHour = cutoffHour;
        this.transitBusinessDays = transitBusinessDays;
    }

    public LocalDate calculateEstimatedDeliveryDate(
            LocalDateTime orderDate) {

        LocalDate deliveryStart = orderDate.toLocalDate();

        // Orders placed after the cutoff start processing
        // on the next business day.
        if (orderDate.getHour() >= cutoffHour) {
            deliveryStart = deliveryStart.plusDays(1);
        }

        // Skip weekends.
        while (isWeekend(deliveryStart)) {
            deliveryStart = deliveryStart.plusDays(1);
        }

        // Add the estimated transit business days.
        int daysAdded = 0;

        while (daysAdded < transitBusinessDays) {

            deliveryStart = deliveryStart.plusDays(1);

            if (!isWeekend(deliveryStart)) {
                daysAdded++;
            }
        }

        return deliveryStart;
    }

    private boolean isWeekend(LocalDate date) {

        DayOfWeek day = date.getDayOfWeek();

        return day == DayOfWeek.SATURDAY
                || day == DayOfWeek.SUNDAY;
    }
}
