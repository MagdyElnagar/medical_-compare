package com.example.medical_compare;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    // يمكنك إضافة استعلامات مخصصة هنا مستقبلاً
}
