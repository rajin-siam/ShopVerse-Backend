package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.User;
import com.ecommerce.project.model.UserProfile;
import com.ecommerce.project.repositories.UserProfileRepository;
import com.ecommerce.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfile getUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    @Transactional
    public UserProfile createUserProfile(Long userId, UserProfile userProfile) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userProfile.setUser(user);
        return userProfileRepository.save(userProfile);
    }

    @Transactional
    public UserProfile updateUserProfile(Long userId, UserProfile userProfileDetails) {
        UserProfile existingProfile = userProfileRepository.findByUserId(userId);

        if (existingProfile == null) {
            return createUserProfile(userId, userProfileDetails);
        }
        existingProfile.setFullName(userProfileDetails.getFullName());
        existingProfile.setGender(userProfileDetails.getGender());
        existingProfile.setPhone(userProfileDetails.getPhone());
        existingProfile.setDateOfBirth(userProfileDetails.getDateOfBirth());
        return userProfileRepository.save(existingProfile);
    }
}