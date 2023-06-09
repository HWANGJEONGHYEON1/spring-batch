package com.example.springbatch.repository;

import com.example.springbatch.entity.Sales;
import com.example.springbatch.entity.SalesSum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesSumRepository extends JpaRepository<SalesSum, Long> {

}
