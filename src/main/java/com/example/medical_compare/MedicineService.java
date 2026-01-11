package com.example.medical_compare;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import ch.qos.logback.core.boolex.Matcher;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class MedicineService {

	// قائمة الكلمات التي يجب حذفها لأنها تغير اسم الصنف بدون داعي
	private static final List<String> STOP_WORDS = Arrays.asList("جديد", "سعر", "مخزن", "عرض", "قديم", "مميز", "تفتيح",
			"كريم", "اقراص", "قرص", "شراب", "ج", "س ج", "باكت", "مجم", "قـــــرص", "جديد", "الباكت", "باكو", "الباكو",
			"بالضمان", "كرتونه", "س  ج", "ك31", "ك81", "40", "48", "ك240", "ك120", "ك420", "ك160", "ك96", "ك80", "ك300",
			"ك590", "بالضمان");

	public String cleanMedicineName(String name) {
		if (name == null)
			return "";

		// إزالة التطويل والرموز
		String clean = name.replaceAll("ـ", "").replaceAll("[أإآ]", "ا").replaceAll("ة", "ه").replaceAll("ى", "ي")
				.replaceAll("[/\\*\\-]", " ").replaceAll("\\s+", " ").replaceAll("\\b(.xlx|.xlsx|.xls)\\b", " ").trim();

		return clean;
	}

	public Medicine parseExcelRow(String rawName, Double price, Double discount, String warehouse) {
		Medicine med = new Medicine();
		med.setRawName(rawName);
		med.setPrice(price);
		med.setDiscount(discount);
		med.setWarehouse(warehouse);

		// 1. استخراج التركيز (الارقام المركبة)
		String regex = "\\d+(\\.\\d+)?(/\\d+(\\.\\d+)?)*";
		Pattern pattern = Pattern.compile(regex);
		java.util.regex.Matcher matcher = pattern.matcher(rawName);
		rawName = truncateAfterUnit(rawName);

		/*
		 * String strengthFound = ""; if (matcher.find()) { strengthFound =
		 * matcher.group(); med.setStrength(strengthFound); }
		 * 
		 * // 2. تنظيف الاسم التجاري (Brand Name) // أولاً: استبدال التركيز بمسافة (لحل
		 * المشكلة اللي ذكرتها حضرتك) String brandName = rawName;
		 * 
		 * // ثانياً: توحيد الحروف وحذف الرموز والكلمات المهملة brandName =
		 * brandName.toLowerCase().replaceAll("[أإآ]", "ا").replaceAll("ة",
		 * "ه").replaceAll("ى", "ي") .replaceAll("[/\\*\\-\\!\\(\\)]", " "); // حذف
		 * الرموز
		 * 
		 * if (!strengthFound.isEmpty()) { brandName = rawName.replace(strengthFound,
		 * " "); // وضعنا مسافة هنا }
		 * 
		 * // ثالثاً: حذف الكلمات المهملة (Stop Words) for (String word : STOP_WORDS) {
		 * brandName = brandName.replaceAll("\\b" + word + "\\b", " "); }
		 * 
		 * // رابعاً: تنظيف المسافات الزائدة brandName = brandName.replaceAll("\\s+",
		 * " ").trim();
		 */
		med.setBrandName(rawName);
		return med;
	}

	private String truncateAfterUnit(String name) {
		if (name == null)
			return "";

		// Regex يبحث عن رقم يليه مباشرة أو بمسافة وحدة قياس (عربي أو إنجليزي)
		// الوحدات: مجم، مل، جم، مج، mg, ml, gm, g
		String regex = "(\\d+(\\.\\d+)?\\s*(مجم|مل|جم|مج|mg|ml|gm|g|mcg))";
		Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CASE);
		java.util.regex.Matcher matcher = pattern.matcher(name);

		if (matcher.find()) {
			// matcher.end() تعطينا موقع نهاية الوحدة (مثلاً بعد حرف الميم في مجم)
			// سنأخذ النص من البداية وحتى هذا الموقع فقط
			return name.substring(0, matcher.end()).trim();
		}

		return name; // إذا لم يجد وحدة، يعيد الاسم كما هو
	}

	private String extractStrength(String name) {
		String regex = "\\d+(\\.\\d+)?(/\\d+(\\.\\d+)?)*";
		Pattern pattern = Pattern.compile(regex);
		java.util.regex.Matcher matcher = pattern.matcher(name);
		return matcher.find() ? matcher.group() : "";
	}

}