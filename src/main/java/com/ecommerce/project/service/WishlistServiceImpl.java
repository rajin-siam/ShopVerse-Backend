package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.WishlistDTO;
import com.ecommerce.project.payload.WishlistItemDTO;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.repositories.WishlistItemRepository;
import com.ecommerce.project.repositories.WishlistRepository;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {
/*
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AuthUtil authUtil;
*/
    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private WishlistItemRepository wishlistItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuthUtil authUtil;



    @Override
    public WishlistDTO getWishlistForUser() {
        User currentUser = getCurrentUser();
        Wishlist wishlist = getOrCreateWishlist(currentUser);
        return convertToWishlistDTO(wishlist);
    }
    @Override
    public boolean isProductInWishlist(Long productId){
        User currentUser = getCurrentUser();
        Wishlist wishlist = getOrCreateWishlist(currentUser);
        return wishlist.getWishlistItems().stream()
                .anyMatch(item -> productId.equals(item.getProduct().getProductId()));
    }

    @Override
    @Transactional
    public WishlistDTO addProductToWishlist(Long productId) {
        User currentUser = getCurrentUser();
        Wishlist wishlist = getOrCreateWishlist(currentUser);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if product already exists in wishlist
        boolean productExists = wishlist.getWishlistItems().stream()
                .anyMatch(item -> item.getProduct().getProductId().equals(productId));

        if (!productExists) {
            WishlistItem wishlistItem = new WishlistItem();
            wishlistItem.setProduct(product);
            wishlistItem.setWishlist(wishlist);

            wishlist.getWishlistItems().add(wishlistItem);
            wishlistRepository.save(wishlist);
        }

        return convertToWishlistDTO(wishlist);
    }

    @Override
    @Transactional
    public void removeProductFromWishlist(Long productId) {
        User currentUser = getCurrentUser();
        Wishlist wishlist = getOrCreateWishlist(currentUser);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        WishlistItem itemToRemove = wishlistItemRepository.findByWishlistAndProduct(wishlist, product)
                .orElseThrow(() -> new APIException("Product not found in wishlist"));

        wishlist.getWishlistItems().remove(itemToRemove);
        wishlistItemRepository.delete(itemToRemove);
        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void clearWishlist() {
        User currentUser = getCurrentUser();
        Wishlist wishlist = getOrCreateWishlist(currentUser);

        wishlist.getWishlistItems().clear();
        wishlistRepository.save(wishlist);
    }

    private User getCurrentUser() {
        String username = authUtil.getCurrentUsername();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new APIException("User not found or not authenticated"));
    }

    private Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    return wishlistRepository.save(newWishlist);
                });
    }

    private WishlistDTO convertToWishlistDTO(Wishlist wishlist) {
        WishlistDTO wishlistDTO = new WishlistDTO();
        wishlistDTO.setWishlistId(wishlist.getWishlistId());
        wishlistDTO.setUserId(wishlist.getUser().getUserId());

        List<WishlistItemDTO> itemDTOs = wishlist.getWishlistItems().stream()
                .map(this::convertToWishlistItemDTO)
                .collect(Collectors.toList());

        wishlistDTO.setWishlistItems(itemDTOs);
        return wishlistDTO;
    }

    private WishlistItemDTO convertToWishlistItemDTO(WishlistItem wishlistItem) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setWishlistItemId(wishlistItem.getWishlistItemId());


        // Convert Product to ProductDTO
        Product product = wishlistItem.getProduct();
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(product.getProductId());
        productDTO.setProductName(product.getProductName());
        productDTO.setDescription(product.getDescription());
        productDTO.setImage(product.getImage());
        productDTO.setPrice(product.getPrice());
        productDTO.setDiscount(product.getDiscount());
        productDTO.setSpecialPrice(product.getSpecialPrice());
        productDTO.setQuantity(product.getQuantity());

        dto.setProduct(productDTO);
        return dto;
    }
}