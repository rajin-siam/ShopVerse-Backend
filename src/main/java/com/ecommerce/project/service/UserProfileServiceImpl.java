package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.User;
import com.ecommerce.project.model.UserProfile;
import com.ecommerce.project.payload.UserProfileDTO;
import com.ecommerce.project.repositories.UserProfileRepository;
import com.ecommerce.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        return modelMapper.map(userProfileRepository.findByUserId(userId),  UserProfileDTO.class);
    }

    @Transactional
    public UserProfileDTO createUserProfile(Long userId, UserProfileDTO userProfileDTO) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        UserProfile userProfile  = modelMapper.map(userProfileDTO, UserProfile.class);
        userProfile.setUser(user);
        return modelMapper.map(userProfileRepository.save(userProfile), UserProfileDTO.class);
    }

    @Transactional
    public UserProfileDTO updateUserProfile(Long userId, UserProfileDTO userProfileDetailsDTO) {
        UserProfile existingProfile = userProfileRepository.findByUserId(userId);


        UserProfile userProfileDetails = modelMapper.map(userProfileDetailsDTO,UserProfile.class);
        if (existingProfile == null) {
            return createUserProfile(userId, userProfileDetailsDTO);
        }
        existingProfile.setFullName(userProfileDetails.getFullName());
        existingProfile.setGender(userProfileDetails.getGender());
        existingProfile.setPhone(userProfileDetails.getPhone());
        existingProfile.setDateOfBirth(userProfileDetails.getDateOfBirth());
        return modelMapper.map(userProfileRepository.save(existingProfile), UserProfileDTO.class);
    }
}