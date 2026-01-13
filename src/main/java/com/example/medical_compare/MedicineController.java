package com.example.medical_compare;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

@Controller
public class MedicineController {

	@Autowired
	private MedicineRepository repository;
	@Autowired
	private MedicineService service;

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@PostMapping("/upload")
	public String uploadMultipleExcel(@RequestParam("files") MultipartFile[] files) throws Exception {
		for (MultipartFile file : files) {
			if (file.isEmpty())
				continue;

			String warehouseName = service.cleanMedicineName(file.getOriginalFilename());

			// 1. مسح البيانات القديمة لهذا المخزن فقط قبل الرفع الجديد
			repository.deleteByWarehouse(warehouseName);

			// 2. معالجة الملف كما فعلنا سابقاً
			Workbook workbook = WorkbookFactory.create(file.getInputStream());
			Sheet sheet = workbook.getSheetAt(0);

			List<Medicine> medicines = new ArrayList<>();
			for (Row row : sheet) {
				if (row.getRowNum() == 0)
					continue;
				try {
					// قراءة البيانات (تأكد من ترتيب الأعمدة عندك: اسم، سعر، خصم)
					String name = row.getCell(0).getStringCellValue();
					Double price = row.getCell(1).getNumericCellValue();
					Double discount = row.getCell(2).getNumericCellValue();
	
					medicines.add(
							service.parseExcelRow(service.cleanMedicineName(name), price, discount, warehouseName));
				} catch (Exception e) {
					// سطر خاطئ، أكمل الباقي
				}
			}
			// 3. حفظ البيانات الجديدة للمخزن
			repository.saveAll(medicines);
			workbook.close();
		}
		return "redirect:/comparison";
	}

	@GetMapping("/view-data")
	public String viewData(Model model) {
		model.addAttribute("medicines", repository.findAll());
		return "view-data";
	}

	@GetMapping("/clear")
	public String clearData() {
		repository.deleteAll();
		return "redirect:/";
	}

	@GetMapping("/comparison")
	public String showComparison(Model model) {
		List<Medicine> allMedicines = repository.findAll();
		List<String> warehouses = allMedicines.stream().map(Medicine::getWarehouse).distinct()
				.collect(Collectors.toList());

		// نستخدم LinkedHashMap للحفاظ على ترتيب الإدخال
		Map<String, ComparisonRow> comparisonMap = new LinkedHashMap<>();

		for (Medicine med : allMedicines) {
			// 1. تنظيف الاسم (البراند والتركيز)
			String cleanName = service.cleanMedicineName(med.getBrandName());
			String strength = med.getStrength() != null ? med.getStrength() : "";
			Double price = med.getPrice();

			// 2. البحث عن صنف موجود بنفس السعر ونفس الاسم (تقريباً)
			String bestKey = findMatchByPriceAndName(comparisonMap, cleanName, strength, price);

			if (bestKey != null) {
				// صنف مطابق في السعر والاسم -> ندمج الخصم
				comparisonMap.get(bestKey).getWarehouseDiscounts().put(med.getWarehouse(), med.getDiscount());
			} else {
				// صنف جديد تماماً (سعر مختلف أو اسم بعيد)
				ComparisonRow row = new ComparisonRow();
				row.setBrandName(med.getBrandName());
				row.setStrength(strength);
				row.setPrice(price);
				row.getWarehouseDiscounts().put(med.getWarehouse(), med.getDiscount());

				// المفتاح الجديد يحتوي على السعر لضمان عدم تداخل الأسعار المختلفة
				String newKey = price + "_" + cleanName + "_" + strength;
				comparisonMap.put(newKey, row);
			}
		}

		model.addAttribute("warehouses", warehouses);
		model.addAttribute("comparisonRows", comparisonMap.values());
		return "comparison";
	}

	// الخوارزمية الجديدة: السعر هو الحكم
	private String findMatchByPriceAndName(Map<String, ComparisonRow> map, String name, String strength, Double price) {
		JaroWinklerSimilarity jw = new JaroWinklerSimilarity();

		for (String key : map.keySet()) {
			ComparisonRow existing = map.get(key);

			// الشرط الأول: السعر متطابق تماماً
			boolean isSamePrice = Math.abs(existing.getPrice() - price) < 0.01;

			if (isSamePrice) {
				// الشرط الثاني: الاسم متشابه جداً (أكثر من 85%) بنفس السعر
				double score = jw.apply(existing.getBrandName().toLowerCase(), name.toLowerCase());
				if (score > 0.90) {
					return key;
				}

				

			}
		}
		return null;
	}

}

