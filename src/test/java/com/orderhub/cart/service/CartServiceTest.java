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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private ProductVariant productVariant;
    private CartItem cartItem;
    private CartItemRequest cartItemRequest;
    private UpdateCartItemRequest updateCartItemRequest;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFullName("John Doe");

        cart = new Cart(user);
        cart.setId(1L);

        productVariant = new ProductVariant();
        productVariant.setId(1L);
        productVariant.setSku("TSHIRT-WHITE-M-001");
        productVariant.setColor("White");
        productVariant.setSize("M");
        productVariant.setStockQuantity(10);
        productVariant.setPrice(new BigDecimal("199000"));
        productVariant.setStatus("ACTIVE");

        cartItem = new CartItem(cart, productVariant, 2);
        cartItem.setId(1L);
        cart.addItem(cartItem);

        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductVariantId(1L);
        cartItemRequest.setQuantity(2);

        updateCartItemRequest = new UpdateCartItemRequest();
        updateCartItemRequest.setQuantity(3);

        cartResponse = new CartResponse();
        cartResponse.setId(1L);
        cartResponse.setItems(List.of());
        cartResponse.setTotalAmount(new BigDecimal("398000"));
    }

    @Test
    void getCurrentUserCart_ShouldReturnCartResponse_WhenCartExists() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(cartMapper.toResponse(cart))
                .thenReturn(cartResponse);

        // Act
        CartResponse response = cartService.getCurrentUserCart("john@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("398000"), response.getTotalAmount());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(cartMapper).toResponse(cart);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getCurrentUserCart_ShouldCreateCart_WhenUserHasNoCart() {
        // Arrange
        Cart newCart = new Cart(user);
        newCart.setId(2L);

        CartResponse newCartResponse = new CartResponse();
        newCartResponse.setId(2L);
        newCartResponse.setItems(List.of());
        newCartResponse.setTotalAmount(BigDecimal.ZERO);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(newCart);

        when(cartMapper.toResponse(newCart))
                .thenReturn(newCartResponse);

        // Act
        CartResponse response = cartService.getCurrentUserCart("john@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        assertEquals(user, savedCart.getUser());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(cartMapper).toResponse(newCart);
    }

    @Test
    void addItemToCart_ShouldAddNewItem_WhenVariantNotInCart() {
        // Arrange
        Cart emptyCart = new Cart(user);
        emptyCart.setId(1L);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(emptyCart));

        when(productVariantRepository.findByIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.of(productVariant));

        when(cartItemRepository.findByCartIdAndProductVariantId(emptyCart.getId(), productVariant.getId()))
                .thenReturn(Optional.empty());

        when(cartRepository.save(emptyCart))
                .thenReturn(emptyCart);

        when(cartMapper.toResponse(emptyCart))
                .thenReturn(cartResponse);

        // Act
        CartResponse response = cartService.addItemToCart("john@example.com", cartItemRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1, emptyCart.getItems().size());
        assertEquals(2, emptyCart.getItems().get(0).getQuantity());
        assertEquals(productVariant, emptyCart.getItems().get(0).getProductVariant());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(productVariantRepository).findByIdAndStatus(1L, "ACTIVE");
        verify(cartItemRepository).findByCartIdAndProductVariantId(emptyCart.getId(), productVariant.getId());
        verify(cartRepository).save(emptyCart);
        verify(cartMapper).toResponse(emptyCart);
    }

    @Test
    void addItemToCart_ShouldIncreaseQuantity_WhenVariantAlreadyExistsInCart() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(productVariantRepository.findByIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.of(productVariant));

        when(cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), productVariant.getId()))
                .thenReturn(Optional.of(cartItem));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        when(cartMapper.toResponse(cart))
                .thenReturn(cartResponse);

        // Act
        CartResponse response = cartService.addItemToCart("john@example.com", cartItemRequest);

        // Assert
        assertNotNull(response);
        assertEquals(4, cartItem.getQuantity());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(productVariantRepository).findByIdAndStatus(1L, "ACTIVE");
        verify(cartItemRepository).findByCartIdAndProductVariantId(cart.getId(), productVariant.getId());
        verify(cartRepository).save(cart);
        verify(cartMapper).toResponse(cart);
    }

    @Test
    void addItemToCart_ShouldThrowResourceNotFoundException_WhenProductVariantDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(productVariantRepository.findByIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItemToCart("john@example.com", cartItemRequest)
        );

        assertEquals("Product variant not found", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(productVariantRepository).findByIdAndStatus(1L, "ACTIVE");
        verify(cartItemRepository, never()).findByCartIdAndProductVariantId(anyLong(), anyLong());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addItemToCart_ShouldThrowBusinessException_WhenRequestedQuantityExceedsStock() {
        // Arrange
        cartItemRequest.setQuantity(20);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(productVariantRepository.findByIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.of(productVariant));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItemToCart("john@example.com", cartItemRequest)
        );

        assertEquals("Requested quantity exceeds available stock", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(productVariantRepository).findByIdAndStatus(1L, "ACTIVE");
        verify(cartItemRepository, never()).findByCartIdAndProductVariantId(anyLong(), anyLong());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addItemToCart_ShouldThrowBusinessException_WhenExistingItemTotalQuantityExceedsStock() {
        // Arrange
        cartItem.setQuantity(9);
        cartItemRequest.setQuantity(2);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(cart));

        when(productVariantRepository.findByIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.of(productVariant));

        when(cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), productVariant.getId()))
                .thenReturn(Optional.of(cartItem));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItemToCart("john@example.com", cartItemRequest)
        );

        assertEquals("Requested quantity exceeds available stock", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartRepository).findByUserId(user.getId());
        verify(productVariantRepository).findByIdAndStatus(1L, "ACTIVE");
        verify(cartItemRepository).findByCartIdAndProductVariantId(cart.getId(), productVariant.getId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartItem_ShouldUpdateQuantity_WhenCartItemBelongsToUser() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartItemRepository.findByIdAndCartUserId(1L, user.getId()))
                .thenReturn(Optional.of(cartItem));

        when(cartItemRepository.save(cartItem))
                .thenReturn(cartItem);

        when(cartMapper.toResponse(cart))
                .thenReturn(cartResponse);

        // Act
        CartResponse response = cartService.updateCartItem("john@example.com", 1L, updateCartItemRequest);

        // Assert
        assertNotNull(response);
        assertEquals(3, cartItem.getQuantity());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartItemRepository).findByIdAndCartUserId(1L, user.getId());
        verify(cartItemRepository).save(cartItem);
        verify(cartMapper).toResponse(cart);
    }

    @Test
    void updateCartItem_ShouldThrowResourceNotFoundException_WhenCartItemDoesNotBelongToUser() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartItemRepository.findByIdAndCartUserId(999L, user.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.updateCartItem("john@example.com", 999L, updateCartItemRequest)
        );

        assertEquals("Cart item not found", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartItemRepository).findByIdAndCartUserId(999L, user.getId());
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartMapper, never()).toResponse(any(Cart.class));
    }

    @Test
    void updateCartItem_ShouldThrowBusinessException_WhenRequestedQuantityExceedsStock() {
        // Arrange
        updateCartItemRequest.setQuantity(20);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartItemRepository.findByIdAndCartUserId(1L, user.getId()))
                .thenReturn(Optional.of(cartItem));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateCartItem("john@example.com", 1L, updateCartItemRequest)
        );

        assertEquals("Requested quantity exceeds available stock", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartItemRepository).findByIdAndCartUserId(1L, user.getId());
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartMapper, never()).toResponse(any(Cart.class));
    }

    @Test
    void removeCartItem_ShouldRemoveItem_WhenCartItemBelongsToUser() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartItemRepository.findByIdAndCartUserId(1L, user.getId()))
                .thenReturn(Optional.of(cartItem));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        when(cartMapper.toResponse(cart))
                .thenReturn(cartResponse);

        // Act
        CartResponse response = cartService.removeCartItem("john@example.com", 1L);

        // Assert
        assertNotNull(response);
        assertFalse(cart.getItems().contains(cartItem));
        assertNull(cartItem.getCart());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartItemRepository).findByIdAndCartUserId(1L, user.getId());
        verify(cartRepository).save(cart);
        verify(cartMapper).toResponse(cart);
    }

    @Test
    void removeCartItem_ShouldThrowResourceNotFoundException_WhenCartItemDoesNotBelongToUser() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(cartItemRepository.findByIdAndCartUserId(999L, user.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeCartItem("john@example.com", 999L)
        );

        assertEquals("Cart item not found", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(cartItemRepository).findByIdAndCartUserId(999L, user.getId());
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartMapper, never()).toResponse(any(Cart.class));
    }

    @Test
    void getCurrentUserCart_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCurrentUserCart("unknown@example.com")
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("unknown@example.com");
        verify(cartRepository, never()).findByUserId(anyLong());
        verify(cartMapper, never()).toResponse(any(Cart.class));
    }
}