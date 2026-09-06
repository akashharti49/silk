package com.gld.service.implentation;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gld.dto.Cart;
import com.gld.dto.CartItem;
import com.gld.dto.Saree;
import com.gld.dto.Silk;
import com.gld.repository.CartItemRepository;
import com.gld.repository.CartRepository;
import com.gld.repository.SareeRepository;
import com.gld.repository.SilkRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private SareeRepository sareeRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private SilkRepository silkRepository;


    // =========================================================
    // GET OR CREATE CART
    // =========================================================

    private Cart getOrCreateCart(HttpSession session) {

        String guestCartId =
                (String) session.getAttribute("GUEST_CART_ID");

        if (guestCartId != null) {

            Optional<Cart> optionalCart =
                    cartRepository.findByGuestCartId(guestCartId);

            if (optionalCart.isPresent()) {
                return optionalCart.get();
            }
        }

        return createCart(session);
    }


    // =========================================================
    // CREATE CART
    // =========================================================

    private Cart createCart(HttpSession session) {

        String guestCartId =
                UUID.randomUUID().toString();

        Cart cart = new Cart();

        cart.setGuestCartId(guestCartId);

        cart = cartRepository.save(cart);

        session.setAttribute(
                "GUEST_CART_ID",
                guestCartId
        );

        return cart;
    }


    // =========================================================
    // ADD SAREE TO CART
    // =========================================================

    @Override
    @Transactional
    public String addSareeToCart(
            int id,
            HttpSession session) {

        Optional<Saree> optionalSaree =
                sareeRepository.findById(id);

        if (optionalSaree.isEmpty()) {
            return "Saree not found";
        }

        Saree saree = optionalSaree.get();


        // Check stock
        if (saree.getStock() <= 0) {
            return "Saree is out of stock";
        }


        // Get cart
        Cart cart = getOrCreateCart(session);


        // Check existing item
        Optional<CartItem> existingItem =
                cartItemRepository
                        .findByCartIdAndProductIdAndProductType(
                                cart.getId(),
                                saree.getId(),
                                "SAREE"
                        );


        // =====================================================
        // EXISTING ITEM
        // =====================================================

        if (existingItem.isPresent()) {

            CartItem item =
                    existingItem.get();

            item.setQuantity(
                    item.getQuantity() + 1
            );

            cartItemRepository.save(item);


            // Reduce stock
            saree.setStock(
                    saree.getStock() - 1
            );

            sareeRepository.save(saree);

            return "Saree quantity increased";
        }


        // =====================================================
        // NEW ITEM
        // =====================================================

        CartItem item = new CartItem();

        item.setProductId(
                saree.getId()
        );

        item.setProductType(
                "SAREE"
        );

        item.setProductName(
                saree.getName()
        );

        item.setPrice(
                saree.getPrice()
        );


        // First image
        if (saree.getImageLinks() != null
                && !saree.getImageLinks().isEmpty()) {

            item.setImage(
                    saree.getImageLinks().get(0)
            );
        }


        item.setQuantity(1);

        item.setCart(cart);

        cartItemRepository.save(item);


        // Reduce stock
        saree.setStock(
                saree.getStock() - 1
        );

        sareeRepository.save(saree);

        return "Saree added to cart";
    }


    // =========================================================
    // ADD SILK TO CART
    // =========================================================

    @Override
    @Transactional
    public String addSilkToCart(
            int id,
            HttpSession session) {

        Optional<Silk> optionalSilk =
                silkRepository.findById(id);

        if (optionalSilk.isEmpty()) {
            return "Silk not found";
        }

        Silk silk = optionalSilk.get();


        // Check stock
        if (silk.getStock() <= 0) {
            return "Silk is out of stock";
        }


        // Get cart
        Cart cart = getOrCreateCart(session);


        // Check existing item
        Optional<CartItem> existingItem =
                cartItemRepository
                        .findByCartIdAndProductIdAndProductType(
                                cart.getId(),
                                silk.getId(),
                                "SILK"
                        );


        // =====================================================
        // EXISTING ITEM
        // =====================================================

        if (existingItem.isPresent()) {

            CartItem item =
                    existingItem.get();

            item.setQuantity(
                    item.getQuantity() + 1
            );

            cartItemRepository.save(item);


            // Reduce stock
            silk.setStock(
                    silk.getStock() - 1
            );

            silkRepository.save(silk);

            return "Silk quantity increased";
        }


        // =====================================================
        // NEW ITEM
        // =====================================================

        CartItem item = new CartItem();

        item.setProductId(
                silk.getId()
        );

        item.setProductType(
                "SILK"
        );

        item.setProductName(
                silk.getName()
        );

        item.setPrice(
                silk.getPrice()
        );


        // First image
        if (silk.getImageLinks() != null
                && !silk.getImageLinks().isEmpty()) {

            item.setImage(
                    silk.getImageLinks().get(0)
            );
        }


        item.setQuantity(1);

        item.setCart(cart);

        cartItemRepository.save(item);


        // Reduce stock
        silk.setStock(
                silk.getStock() - 1
        );

        silkRepository.save(silk);

        return "Silk added to cart";
    }


    // =========================================================
    // GET CART
    // =========================================================

    
    public Cart getCart(HttpSession session) {

        return getOrCreateCart(session);
    }


    // =========================================================
    // REMOVE SAREE FROM CART
    // =========================================================

    @Override
    @Transactional
    public String removeSareeFromCart(
            int id,
            HttpSession session) {

    	Cart cart = getOrCreateCart(session);

        Optional<CartItem> optionalItem =
                cartItemRepository
                        .findByCartIdAndProductIdAndProductType(
                                cart.getId(),
                                id,
                                "SAREE"
                        );

        if (optionalItem.isEmpty()) {
            return "Saree not found in cart";
        }

        CartItem item = optionalItem.get();

        // Find Saree
        Optional<Saree> optionalSaree =
                sareeRepository.findById(id);

        if (optionalSaree.isEmpty()) {
            return "Saree not found";
        }

        Saree saree = optionalSaree.get();

        // Return ONLY 1 saree to stock
        saree.setStock(saree.getStock() + 1);

        sareeRepository.save(saree);

        // Reduce cart quantity by 1
        int newQuantity = item.getQuantity() - 1;

        if (newQuantity <= 0) {

            // Quantity reached 0 → remove cart item
            cartItemRepository.delete(item);

            return "Saree removed from cart";
        }

        // Otherwise update quantity
        item.setQuantity(newQuantity);

        cartItemRepository.save(item);

        return "Saree quantity decreased";
    }


    // =========================================================
    // REMOVE SILK FROM CART
    // =========================================================

    @Override
    @Transactional
    public String removeSilkFromCart(
            int id,
            HttpSession session) {
    	Cart cart = getOrCreateCart(session);

        Optional<CartItem> optionalItem =
                cartItemRepository
                        .findByCartIdAndProductIdAndProductType(
                                cart.getId(),
                                id,
                                "SILK"
                        );

        if (optionalItem.isEmpty()) {
            return "Silk not found in cart";
        }

        CartItem item = optionalItem.get();

        // Find Silk
        Optional<Silk> optionalSilk =
                silkRepository.findById(id);

        if (optionalSilk.isEmpty()) {
            return "Silk not found";
        }

        Silk silk = optionalSilk.get();

        // Return ONLY 1 item to stock
        silk.setStock(silk.getStock() + 1);

        silkRepository.save(silk);

        // Reduce cart quantity by 1
        int newQuantity = item.getQuantity() - 1;

        if (newQuantity <= 0) {

            // Quantity reached 0 → remove cart item
            cartItemRepository.delete(item);

            return "Silk removed from cart";
        }

        // Otherwise update quantity
        item.setQuantity(newQuantity);

        cartItemRepository.save(item);

        return "Silk quantity decreased";
    } 

    
}

