package com.gld.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gld.dto.CartItem;
import com.gld.dto.Saree;

public interface CartItemRepository extends JpaRepository<CartItem, Integer>{

	Optional<CartItem> findByCartIdAndProductIdAndProductType(int id, int id2, String string);

	
}
