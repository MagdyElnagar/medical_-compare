package com.example.medical_compare;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    
    @Transactional
    @Modifying
    void deleteByWarehouse(String warehouse);
}