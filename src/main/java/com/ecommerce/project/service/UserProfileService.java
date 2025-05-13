package com.ecommerce.project.service;

import com.ecommerce.project.model.UserProfile;
import com.ecommerce.project.payload.UserProfileDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface UserProfileService {


    @Transactional(readOnly = true)
    UserProfileDTO getUserProfile(Long userId);

    @Transactional
    UserProfileDTO createUserProfile(Long userId, UserProfileDTO userProfile);

    @Transactional
    UserProfileDTO updateUserProfile(Long userId, UserProfileDTO userProfileDetails);
}