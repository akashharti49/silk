package com.gld.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class CartItem {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private int id;

	    // Saree.id OR Silk.id
	    private int productId;

	    // SAREE or SILK
	    private String productType;

	    // Saree.name OR Silk.name
	    private String productName;

	    // Saree.price OR Silk.price
	    private double price;

	    // First image from imageLinks
	    private String image;

	    // Quantity added to cart
	    private int quantity;

	    @ManyToOne
	    @JoinColumn(name = "cart_id")
	    private Cart cart;

}
