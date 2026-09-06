package com.gld.service.implentation;

import java.util.List;

import com.gld.dto.Order;

public interface OrderService {

	List<Order> getOrdersByEmail(String email);

	Order getOrderById(int id);

	/**
	 * Customer initiated cancellation. The email must match the order's
	 * email so a random person cannot cancel someone else's order.
	 */
	Order cancelOrderByCustomer(int orderId, String email, String reason) throws Exception;

	// ===================== ADMIN =====================

	List<Order> getAllOrders();

	Order cancelOrderByAdmin(int orderId, String reason) throws Exception;

	Order updateOrderStatus(int orderId, String status) throws Exception;

}
