package com.gld.service.implentation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gld.dto.Order;
import com.gld.helper.MailService;
import com.gld.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

	// Orders in these statuses can no longer be cancelled.
	private static final List<String> NON_CANCELLABLE = List.of("CANCELLED", "DELIVERED");

	// Valid forward-moving order tracking stages (excludes CANCELLED, which
	// has its own dedicated flow).
	private static final List<String> VALID_STATUSES =
			List.of("PLACED", "CONFIRMED", "PACKED", "SHIPPED", "DELIVERED");

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private MailService mailService;

	@Override
	public List<Order> getOrdersByEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			return List.of();
		}
		return orderRepository.findByEmailIgnoreCaseOrderByOrderDateDesc(email.trim());
	}

	@Override
	public Order getOrderById(int id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new IllegalStateException("Order not found."));
	}

	@Override
	@Transactional
	public Order cancelOrderByCustomer(int orderId, String email, String reason) throws Exception {

		Order order = getOrderById(orderId);

		if (email == null || !order.getEmail().equalsIgnoreCase(email.trim())) {
			throw new IllegalStateException("We could not verify this order for that email address.");
		}

		validateCancellable(order);

		order.setStatus("CANCELLED");
		order.setCancelReason(reason);
		order.setCancelledBy("CUSTOMER");
		order.setCancelledAt(LocalDateTime.now());

		order = orderRepository.save(order);

		try {
			mailService.sendCancellationEmail(
					order.getEmail(), order.getCustomerName(), order.getOrderNumber(), reason);
		} catch (Exception e) {
			// Cancellation already went through; don't fail just because the email couldn't be sent.
		}

		return order;
	}

	@Override
	public List<Order> getAllOrders() {
		return orderRepository.findAllByOrderByOrderDateDesc();
	}

	@Override
	@Transactional
	public Order cancelOrderByAdmin(int orderId, String reason) throws Exception {

		Order order = getOrderById(orderId);

		validateCancellable(order);

		order.setStatus("CANCELLED");
		order.setCancelReason(reason);
		order.setCancelledBy("ADMIN");
		order.setCancelledAt(LocalDateTime.now());

		order = orderRepository.save(order);

		try {
			mailService.sendCancellationEmail(
					order.getEmail(), order.getCustomerName(), order.getOrderNumber(), reason);
		} catch (Exception e) {
			// Cancellation already went through; don't fail just because the email couldn't be sent.
		}

		return order;
	}

	@Override
	@Transactional
	public Order updateOrderStatus(int orderId, String status) throws Exception {

		Order order = getOrderById(orderId);

		if (NON_CANCELLABLE.contains(order.getStatus())) {
			throw new IllegalStateException("This order is " + order.getStatus().toLowerCase()
					+ " and its status can no longer be changed.");
		}

		if (status == null || !VALID_STATUSES.contains(status)) {
			throw new IllegalArgumentException("Invalid order status: " + status);
		}

		order.setStatus(status);
		stampTrackingTimeline(order, status);

		order = orderRepository.save(order);

		try {
			mailService.sendStatusUpdateEmail(
					order.getEmail(), order.getCustomerName(), order.getOrderNumber(), status);
		} catch (Exception e) {
			// Status update already went through; don't fail just because the email couldn't be sent.
		}

		return order;
	}

	/**
	 * Stamps the timestamp for the stage the order just reached, and
	 * back-fills any earlier stage timestamps that are still empty (in case
	 * the admin jumps straight to a later stage) so the tracking timeline
	 * never shows a gap.
	 */
	private void stampTrackingTimeline(Order order, String status) {

		LocalDateTime now = LocalDateTime.now();
		int reachedStep = order.getProgressStep();

		if (reachedStep >= 2 && order.getConfirmedAt() == null) {
			order.setConfirmedAt(now);
		}
		if (reachedStep >= 3 && order.getPackedAt() == null) {
			order.setPackedAt(now);
		}
		if (reachedStep >= 4 && order.getShippedAt() == null) {
			order.setShippedAt(now);
		}
		if (reachedStep >= 5 && order.getDeliveredAt() == null) {
			order.setDeliveredAt(now);
		}
	}

	private void validateCancellable(Order order) {
		if (NON_CANCELLABLE.contains(order.getStatus())) {
			throw new IllegalStateException(
					"This order is already " + order.getStatus().toLowerCase() + " and cannot be cancelled.");
		}
	}

}
