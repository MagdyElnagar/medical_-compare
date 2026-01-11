package com.example.medical_compare;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ComparisonRow {
    private String brandName;
    private String strength;
    private Double price;
    // Map: مفتاحها اسم المخزن وقيمتها الخصم
    private Map<String, Double> warehouseDiscounts = new HashMap<>();
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getStrength() {
		return strength;
	}
	public void setStrength(String strength) {
		this.strength = strength;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Map<String, Double> getWarehouseDiscounts() {
		return warehouseDiscounts;
	}
	public void setWarehouseDiscounts(Map<String, Double> warehouseDiscounts) {
		this.warehouseDiscounts = warehouseDiscounts;
	}
}