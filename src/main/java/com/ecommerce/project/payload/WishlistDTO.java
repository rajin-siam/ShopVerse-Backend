package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {
    private Long wishlistId;
    private Long userId;
    private List<WishlistItemDTO> wishlistItems = new ArrayList<>();
}