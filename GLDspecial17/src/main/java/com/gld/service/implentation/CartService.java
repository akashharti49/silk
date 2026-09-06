package com.gld.service.implentation;

import com.gld.dto.Cart;

import jakarta.servlet.http.HttpSession;

public interface CartService {

	String addSareeToCart(int id, HttpSession session);

	String addSilkToCart(int id, HttpSession session);

	String removeSilkFromCart(int id, HttpSession session);

	String removeSareeFromCart(int id, HttpSession session);

	Cart getCart(HttpSession session);


}
