package com.gld.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gld.dto.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

	Order findByOrderNumber(String orderNumber);

	List<Order> findByEmailIgnoreCaseOrderByOrderDateDesc(String email);

	List<Order> findAllByOrderByOrderDateDesc();

}
