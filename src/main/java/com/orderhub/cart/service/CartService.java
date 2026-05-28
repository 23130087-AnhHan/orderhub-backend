package com.orderhub.cart.service;

import com.orderhub.cart.dto.CartItemRequest;
import com.orderhub.cart.dto.CartResponse;
import com.orderhub.cart.dto.UpdateCartItemRequest;
import com.orderhub.cart.entity.Cart;
import com.orderhub.cart.entity.CartItem;
import com.orderhub.cart.mapper.CartMapper;
import com.orderhub.cart.repository.CartItemRepository;
import com.orderhub.cart.repository.CartRepository;
import com.orderhub.common.exception.BusinessException;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.product.entity.ProductVariant;
import com.orderhub.product.repository.ProductVariantRepository;
import com.orderhub.user.entity.User;
import com.orderhub.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductVariantRepository productVariantRepository,
            UserRepository userRepository,
            CartMapper cartMapper
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.cartMapper = cartMapper;
    }

    public CartResponse getCurrentUserCart(String email) {
        User user = getUserByEmail(email);
        Cart cart = getOrCreateCart(user);

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse addItemToCart(String email, CartItemRequest request) {
        User user = getUserByEmail(email);
        Cart cart = getOrCreateCart(user);

        ProductVariant productVariant = productVariantRepository
                .findByIdAndStatus(request.getProductVariantId(), "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

        if (request.getQuantity() > productVariant.getStockQuantity()) {
            throw new BusinessException("Requested quantity exceeds available stock");
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductVariantId(cart.getId(), productVariant.getId())
                .orElse(null);

        if (cartItem == null) {
            cartItem = new CartItem(cart, productVariant, request.getQuantity());
            cart.addItem(cartItem);
        } else {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > productVariant.getStockQuantity()) {
                throw new BusinessException("Requested quantity exceeds available stock");
            }

            cartItem.setQuantity(newQuantity);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Transactional
    public CartResponse updateCartItem(String email, Long cartItemId, UpdateCartItemRequest request) {
        User user = getUserByEmail(email);

        CartItem cartItem = cartItemRepository
                .findByIdAndCartUserId(cartItemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        ProductVariant productVariant = cartItem.getProductVariant();

        if (request.getQuantity() > productVariant.getStockQuantity()) {
            throw new BusinessException("Requested quantity exceeds available stock");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(String email, Long cartItemId) {
        User user = getUserByEmail(email);

        CartItem cartItem = cartItemRepository
                .findByIdAndCartUserId(cartItemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }
}