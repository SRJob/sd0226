package com.cardinal.financials.tools.rental;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RentalCalculator {
    private static final Logger logger = Logger.getLogger(RentalCalculator.class.getName());

    private static final Map<String, ToolRate> TOOL_RATES = new HashMap<>();
    static {
        // Tool rates for each tool code
        TOOL_RATES.put("LADW", new ToolRate("Ladder", "Werner", 1.99, true, true, false));
        TOOL_RATES.put("CHNS", new ToolRate("Chainsaw", "Stihl", 1.49, true, false, true));
        TOOL_RATES.put("JAKD", new ToolRate("Jackhammer", "DeWalt", 2.99, true, false, false));
        TOOL_RATES.put("JAKR", new ToolRate("Jackhammer", "Ridgid", 2.99, true, false, false));
    }

    private static final LocalDate INDEPENDENCE_DAY = LocalDate.of(LocalDate.now().getYear(), 7, 4);
    private static final LocalDate LABOR_DAY = getLaborDay(LocalDate.now().getYear());

    public static RentalAgreement calculateRentalCost(String toolCode, int rentalDayCount, double discount, LocalDate checkOutDate) {
        try {
            if (rentalDayCount < 1) {
                throw new IllegalArgumentException("Rental day count must be 1 or greater.");
            }
            if (discount < 0 || discount > 100) {
                throw new IllegalArgumentException("Discount percent must be in the range 0-100.");
            }
            if (!TOOL_RATES.containsKey(toolCode)) {
                throw new IllegalArgumentException("Invalid tool code");
            }

            ToolRate toolRate = TOOL_RATES.get(toolCode);
            double dailyRate = toolRate.getDailyCharge();

            LocalDate dueDate = checkOutDate.plusDays(rentalDayCount);

            int chargeDays = calculateChargeableDays(checkOutDate, dueDate, toolRate);

            double preDiscountCharge = chargeDays * dailyRate;

            BigDecimal preDiscountChargeRounded = BigDecimal.valueOf(preDiscountCharge).setScale(2, BigDecimal.ROUND_HALF_UP);

            double discountAmount = (discount / 100.0) * preDiscountCharge;

            BigDecimal discountAmountRounded = BigDecimal.valueOf(discountAmount).setScale(2, BigDecimal.ROUND_HALF_UP);

            BigDecimal finalCharge = preDiscountChargeRounded.subtract(discountAmountRounded);

            return new RentalAgreement(toolCode, toolRate.getToolType(), toolRate.getBrand(), rentalDayCount, checkOutDate, dueDate, dailyRate, chargeDays, preDiscountChargeRounded, discount, discountAmountRounded, finalCharge);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Invalid input parameters: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating rental cost", e);
            throw new RuntimeException("Error calculating rental cost", e);
        }
    }

    private static int calculateChargeableDays(LocalDate checkOutDate, LocalDate dueDate, ToolRate toolRate) {
        int chargeDays = 0;
        LocalDate currentDate = checkOutDate.plusDays(1); // Start from the day after checkout
        while (!currentDate.isAfter(dueDate)) {
            if (isChargeableDay(currentDate, toolRate)) {
                chargeDays++;
            }
            currentDate = currentDate.plusDays(1);
        }
        return chargeDays;
    }

    private static boolean isChargeableDay(LocalDate date, ToolRate toolRate) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        boolean isHoliday = isIndependenceDay(date) || isLaborDay(date);

        return (isWeekend && toolRate.isWeekendCharge()) || (!isWeekend && toolRate.isWeekdayCharge() && !isHoliday);
    }

    private static boolean isIndependenceDay(LocalDate date) {
        return (date.getMonthValue() == 7 && date.getDayOfMonth() == 4) && !date.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !date.getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

    private static boolean isLaborDay(LocalDate date) {
        return date.equals(LABOR_DAY);
    }

    private static LocalDate getLaborDay(int year) {
        LocalDate laborDay = LocalDate.of(year, 9, 1);
        while (!laborDay.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            laborDay = laborDay.plusDays(1);
        }
        return laborDay;
    }
    
    public static void printToolsInfo() {
    	System.out.println("================================================");
        System.out.println("Tool Code\tTool Type\tBrand");
        System.out.println("-------------------------------------");
        for (Map.Entry<String, ToolRate> entry : TOOL_RATES.entrySet()) {
            String toolCode = entry.getKey();
            ToolRate toolRate = entry.getValue();
            System.out.println(toolCode + "\t\t" + toolRate.getToolType() + "\t\t" + toolRate.getBrand());
        }
    }

    public static void main(String[] args) {
        String toolCode = "LADW";
        int rentalDayCount = 5;
        double discount = 10; // 10% discount
        LocalDate checkOutDate = LocalDate.now();
        printToolsInfo();
        System.out.println("================================================");
        System.out.println("Rental Agreement");
        System.out.println("----------------");
        try {
            RentalAgreement rentalAgreement = calculateRentalCost(toolCode, rentalDayCount, discount, checkOutDate);
            System.out.println(rentalAgreement);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("================================================");
    }
}

class RentalAgreement {
    private String toolCode;
    private String toolType;
    private String toolBrand;
    private int rentalDays;
    private LocalDate checkOutDate;
    private LocalDate dueDate;
    private double dailyRentalCharge;
    private int chargeDays;
    private BigDecimal preDiscountCharge;
    private double discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal finalCharge;

    public RentalAgreement(String toolCode, String toolType, String toolBrand, int rentalDays, LocalDate checkOutDate,
                           LocalDate dueDate, double dailyRentalCharge, int chargeDays, BigDecimal preDiscountCharge,
                           double discountPercent, BigDecimal discountAmount, BigDecimal finalCharge) {
        this.toolCode = toolCode;
        this.toolType = toolType;
        this.toolBrand = toolBrand;
        this.rentalDays = rentalDays;
        this.checkOutDate = checkOutDate;
        this.dueDate = dueDate;
        this.dailyRentalCharge = dailyRentalCharge;
        this.chargeDays = chargeDays;
        this.preDiscountCharge = preDiscountCharge;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.finalCharge = finalCharge;
    }

    public String getToolCode() {
        return toolCode;
    }

    public String getToolType() {
        return toolType;
    }

    public String getToolBrand() {
        return toolBrand;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public double getDailyRentalCharge() {
        return dailyRentalCharge;
    }

    public int getChargeDays() {
        return chargeDays;
    }

    public BigDecimal getPreDiscountCharge() {
        return preDiscountCharge;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getFinalCharge() {
        return finalCharge;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return "Tool code: " + toolCode + "\n" +
                "Tool type: " + toolType + "\n" +
                "Tool brand: " + toolBrand + "\n" +
                "Rental days: " + rentalDays + "\n" +
                "Check out date: " + checkOutDate.format(formatter) + "\n" +
                "Due date: " + dueDate.format(formatter) + "\n" +
                "Daily rental charge: $" + dailyRentalCharge + "\n" +
                "Charge days: " + chargeDays + "\n" +
                "Pre-discount charge: $" + preDiscountCharge + "\n" +
                "Discount percent: " + discountPercent + "%\n" +
                "Discount amount: $" + discountAmount + "\n" +
                "Final charge: $" + finalCharge;
    }
}

class ToolRate {
    private double dailyCharge;
    private boolean weekdayCharge;
    private boolean weekendCharge;
    private boolean holidayCharge;
    private String brand;
    private String toolType;
    
    public String getToolType() {
		return toolType;
	}

	public void setToolType(String toolType) {
		this.toolType = toolType;
	}

	public ToolRate(String toolType, String brand, double dailyCharge, boolean weekdayCharge, boolean weekendCharge, boolean holidayCharge) {
        this.toolType = toolType;
        this.brand = brand;
        this.dailyCharge = dailyCharge;
        this.weekdayCharge = weekdayCharge;
        this.weekendCharge = weekendCharge;
        this.holidayCharge = holidayCharge;
    }
    
    public double getDailyCharge() {
        return dailyCharge;
    }
    
    public boolean isWeekdayCharge() {
        return weekdayCharge;
    }
    
    public boolean isWeekendCharge() {
        return weekendCharge;
    }
    
    public boolean isHolidayCharge() {
        return holidayCharge;
    }
    
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
