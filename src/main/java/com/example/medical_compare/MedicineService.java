package com.example.medical_compare;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		String clean;
		// إزالة التطويل والرموز
		name = name.replaceAll("٠", "0").replaceAll("١", "1").replaceAll("٢", "2").replaceAll("٣", "3")
				.replaceAll("٤", "4").replaceAll("٥", "5").replaceAll("٦", "6").replaceAll("٧", "7")
				.replaceAll("٨", "8").replaceAll("٩", "9").replaceAll(" بلس", " plus ").replaceAll(" بلاس", " plus ").replaceAll(" بلاص", " plus ")
				.replaceAll(" بلاس ", " plus ")
				.trim();
				
		clean = name.replaceAll("ـ", "").replaceAll("[أإآ]", "ا").replaceAll("ة", "ه").replaceAll("ى", "ي")
				.replaceAll("س جديييييييد", "")
				.replaceAll(".xlsx", "").replaceAll(".xls", "").replaceAll("سعر\\d+", "").replaceAll("س ج\\d+", "")
				.replaceAll("(?i) باكو\\s*\\d+", "").replaceAll(" س ج\\d+", "").replaceAll("سعر \\d+", "")
				.replaceAll("شريط", "").replaceAll("شريططط", "").replaceAll("58ج", "").replaceAll("شريط", "")
				.trim();

		clean = clean.replaceAll("سعر جديد", "").replaceAll(" س.ج\\d+", "").replaceAll("ق س", "").replaceAll("سعر قديم", "").replaceAll(" س ق ", "").replaceAll("الاسكندريه", "")
				.replaceAll("س جديد ", "").replaceAll("جديد", "").replaceAll("قديم", "").replaceAll("جديييد", "")
				
				.replaceAll("سعر", "").replaceAll(" س ج ", "").replaceAll("جدييييييييييد", "")
				.replaceAll("جدييييييييييييد", "").replaceAll("جدييييد", "").replaceAll("جدييد", "")
				.replaceAll("جدييييييد", "").replaceAll("81ج", "").replaceAll("(?i)سعر", "")
.replaceAll("اقراص", "tap").replaceAll(" ق ", "tap").replaceAll("قرص", "tap").replaceAll("كبسول", "tap").replaceAll(" ك ", "tap")
				.replaceAll("ك\\d+", "") // إزالة "ك" متبوعة بأرقام
				.replaceAll("فوار", "اكياس").replaceAll("حبيبات", "اكياس").replaceAll("اكس تينشن", "اكستنشن")

				.replaceAll("سعر جديد\\d+", "").replaceAll("س.ج", "").replaceAll(" س ", "")

				.replaceAll("[\\*\\-\\+\\=\\_\\#\\@\\!\\؟\\،\\؛]", " ").replaceAll("[\\*\\-]", " ")
				.replaceAll("\\s+", " ").trim();

		return clean;
	}

	private static final Map<Character, Character> ARABIC_TO_ENGLISH_DIGITS = new HashMap<>();
	static {
		ARABIC_TO_ENGLISH_DIGITS.put('٠', '0');
		ARABIC_TO_ENGLISH_DIGITS.put('١', '1');
		ARABIC_TO_ENGLISH_DIGITS.put('٢', '2');
		ARABIC_TO_ENGLISH_DIGITS.put('٣', '3');
		ARABIC_TO_ENGLISH_DIGITS.put('٤', '4');
		ARABIC_TO_ENGLISH_DIGITS.put('٥', '5');
		ARABIC_TO_ENGLISH_DIGITS.put('٦', '6');
		ARABIC_TO_ENGLISH_DIGITS.put('٧', '7');
		ARABIC_TO_ENGLISH_DIGITS.put('٨', '8');
		ARABIC_TO_ENGLISH_DIGITS.put('٩', '9');
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
		String regex = "(مجم|مل|جم|مج|mg|ml|gm|g|mcg)";

		Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CASE);
		java.util.regex.Matcher matcher = pattern.matcher(name);

		if (matcher.find()) {
			// matcher.end() تعطينا موقع نهاية الوحدة (مثلاً بعد حرف الميم في مجم)
			// سنأخذ النص من البداية وحتى هذا الموقع فقط
			return name.substring(0, matcher.end()).trim();
		}

		return name;// إذا لم يجد وحدة، يعيد الاسم كما هو
	}

	private String extractStrength(String name) {
		String regex = "\\d+(\\.\\d+)?(/\\d+(\\.\\d+)?)*";
		Pattern pattern = Pattern.compile(regex);
		java.util.regex.Matcher matcher = pattern.matcher(name);
		return matcher.find() ? matcher.group() : "";
	}

}

