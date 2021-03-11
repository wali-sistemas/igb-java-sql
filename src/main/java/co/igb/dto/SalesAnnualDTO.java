package co.igb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * @author jguisao
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesAnnualDTO {
    private String year;
    private BigDecimal totalCost;
    private BigDecimal totalSale;
    private BigDecimal margeSale;

    public SalesAnnualDTO() {
    }

    public SalesAnnualDTO(String year, BigDecimal totalCost, BigDecimal totalSale, BigDecimal margeSale) {
        this.year = year;
        this.totalCost = totalCost;
        this.totalSale = totalSale;
        this.margeSale = margeSale;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getTotalSale() {
        return totalSale;
    }

    public void setTotalSale(BigDecimal totalSale) {
        this.totalSale = totalSale;
    }

    public BigDecimal getMargeSale() {
        return margeSale;
    }

    public void setMargeSale(BigDecimal margeSale) {
        this.margeSale = margeSale;
    }

    @Override
    public String toString() {
        return "SalesAnnualDTO{" +
                "year='" + year + '\'' +
                ", totalCost=" + totalCost +
                ", totalSale=" + totalSale +
                ", margeSale=" + margeSale +
                '}';
    }
}