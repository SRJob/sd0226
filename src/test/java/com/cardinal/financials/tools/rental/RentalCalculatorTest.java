package com.cardinal.financials.tools.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

public class RentalCalculatorTest {

    @Test
    public void testWeekdayRentalNoDiscount() {
        String toolCode = "LADW";
        int rentalDayCount = 5; // Weekday rental
        double discount = 0; // No discount
        LocalDate checkOutDate = LocalDate.of(2022, 3, 1); // Tuesday, March 1, 2022

        RentalAgreement rentalAgreement = RentalCalculator.calculateRentalCost(toolCode, rentalDayCount, discount, checkOutDate);

        assertEquals("LADW", rentalAgreement.getToolCode());
        assertEquals("Ladder", rentalAgreement.getToolType());
        assertEquals("Werner", rentalAgreement.getToolBrand());
        assertEquals(5, rentalAgreement.getRentalDays());
        assertEquals(LocalDate.of(2022, 3, 1), rentalAgreement.getCheckOutDate());
        assertEquals(LocalDate.of(2022, 3, 6), rentalAgreement.getDueDate());
        assertEquals(1.99, rentalAgreement.getDailyRentalCharge());
        assertEquals(5, rentalAgreement.getChargeDays()); // Weekdays only
        assertEquals(new BigDecimal("9.95"), rentalAgreement.getPreDiscountCharge());
        assertEquals(0.0, rentalAgreement.getDiscountPercent());
        assertEquals(BigDecimal.ZERO.setScale(2), rentalAgreement.getDiscountAmount());
        assertEquals(new BigDecimal("9.95"), rentalAgreement.getFinalCharge());
    }

    @Test
    public void testWeekendRentalWithDiscount() {
        String toolCode = "CHNS";
        int rentalDayCount = 2; // Weekend rental
        double discount = 15; // 15% discount
        LocalDate checkOutDate = LocalDate.of(2022, 3, 5); // Saturday, March 5, 2022

        RentalAgreement rentalAgreement = RentalCalculator.calculateRentalCost(toolCode, rentalDayCount, discount, checkOutDate);

        assertEquals("CHNS", rentalAgreement.getToolCode());
        assertEquals("Chainsaw", rentalAgreement.getToolType());
        assertEquals("Stihl", rentalAgreement.getToolBrand());
        assertEquals(2, rentalAgreement.getRentalDays());
        assertEquals(LocalDate.of(2022, 3, 5), rentalAgreement.getCheckOutDate());
        assertEquals(LocalDate.of(2022, 3, 7), rentalAgreement.getDueDate());
        assertEquals(1.49, rentalAgreement.getDailyRentalCharge());
        assertEquals(1, rentalAgreement.getChargeDays()); // Weekend (Sunday)
        assertEquals(new BigDecimal("1.49"), rentalAgreement.getPreDiscountCharge());
        assertEquals(15.0, rentalAgreement.getDiscountPercent());
        assertEquals(new BigDecimal("0.22"), rentalAgreement.getDiscountAmount());
        assertEquals(new BigDecimal("1.27"), rentalAgreement.getFinalCharge());
    }

    @Test
    public void testHolidayRentalNoDiscount() {
        String toolCode = "JAKD";
        int rentalDayCount = 3; // Holiday rental (Independence Day)
        double discount = 0; // No discount
        LocalDate checkOutDate = LocalDate.of(2022, 7, 3); // Sunday, July 3, 2022

        RentalAgreement rentalAgreement = RentalCalculator.calculateRentalCost(toolCode, rentalDayCount, discount, checkOutDate);

        assertEquals("JAKD", rentalAgreement.getToolCode());
        assertEquals("Jackhammer", rentalAgreement.getToolType());
        assertEquals("DeWalt", rentalAgreement.getToolBrand());
        assertEquals(3, rentalAgreement.getRentalDays());
        assertEquals(LocalDate.of(2022, 7, 3), rentalAgreement.getCheckOutDate());
        assertEquals(LocalDate.of(2022, 7, 6), rentalAgreement.getDueDate());
        assertEquals(2.99, rentalAgreement.getDailyRentalCharge());
        assertEquals(2, rentalAgreement.getChargeDays()); // Holiday (Monday) and following day (Tuesday)
        assertEquals(new BigDecimal("5.98"), rentalAgreement.getPreDiscountCharge());
        assertEquals(0.0, rentalAgreement.getDiscountPercent());
        assertEquals(BigDecimal.ZERO.setScale(2), rentalAgreement.getDiscountAmount());
        assertEquals(new BigDecimal("5.98"), rentalAgreement.getFinalCharge());
    }

    @Test
    public void testWeekdayAndWeekendRentalWithDiscount() {
        String toolCode = "LADW";
        int rentalDayCount = 5; // Weekday and weekend rental
        double discount = 20; // 20% discount
        LocalDate checkOutDate = LocalDate.of(2022, 3, 3); // Thursday, March 3, 2022

        RentalAgreement rentalAgreement = RentalCalculator.calculateRentalCost(toolCode, rentalDayCount, discount, checkOutDate);

        assertEquals("LADW", rentalAgreement.getToolCode());
        assertEquals("Ladder", rentalAgreement.getToolType());
        assertEquals("Werner", rentalAgreement.getToolBrand());
        assertEquals(5, rentalAgreement.getRentalDays());
        assertEquals(LocalDate.of(2022, 3, 3), rentalAgreement.getCheckOutDate());
        assertEquals(LocalDate.of(2022, 3, 8), rentalAgreement.getDueDate());
        assertEquals(1.99, rentalAgreement.getDailyRentalCharge());
        assertEquals(5, rentalAgreement.getChargeDays()); // Weekdays only (excluding Friday)
        assertEquals(new BigDecimal("9.95"), rentalAgreement.getPreDiscountCharge());
        assertEquals(20.0, rentalAgreement.getDiscountPercent());
        assertEquals(new BigDecimal("1.99"), rentalAgreement.getDiscountAmount());
        assertEquals(new BigDecimal("7.96"), rentalAgreement.getFinalCharge());
    }

    @Test
    public void testNegativeRentalDayCount() {
        String toolCode = "LADW";
        int rentalDayCount = -1; // Negative rental day count
        double discount = 10;
        LocalDate checkOutDate = LocalDate.now();

        assertThrows(IllegalArgumentException.class, () -> RentalCalculator.calculateRentalCost(toolCode, rentalDayCount, discount, checkOutDate));
    }
}
