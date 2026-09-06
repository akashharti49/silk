package com.gld.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gld.dto.Cart;
import com.gld.dto.Saree;

public interface CartRepository extends JpaRepository<Cart, Integer> {

	Optional<Cart> findByGuestCartId(String guestCartId);

}
