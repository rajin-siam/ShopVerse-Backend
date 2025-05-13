package com.ecommerce.project.service;

import com.ecommerce.project.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface UserProfileService {


    @Transactional(readOnly = true)
    UserProfile getUserProfile(Long userId);

    @Transactional
    UserProfile createUserProfile(Long userId, UserProfile userProfile);

    @Transactional
    UserProfile updateUserProfile(Long userId, UserProfile userProfileDetails);
}