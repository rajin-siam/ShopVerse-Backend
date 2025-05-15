package com.ecommerce.project.security.services;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.AuthProvider;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.googleapis.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class GoogleService {

    private static final String CLIENT_ID = "490184832474-47nbop45u4kcunld04aafg2f40o3ptdv.apps.googleusercontent.com";// Replace with your actual client ID


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User processGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new GooglePublicKeysManager.Builder(Utils.getDefaultTransport(), Utils.getDefaultJsonFactory())
                            .build())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String googleId = payload.getSubject(); // This is the Google user ID

                Optional<User> existingUser = userRepository.findByEmail(email);

                if (existingUser.isPresent()) {
                    User user = existingUser.get();

                    // Update provider details if user was previously registered with local account
                    if (user.getProvider() == AuthProvider.LOCAL) {
                        user.setProvider(AuthProvider.GOOGLE);
                        user.setProviderId(googleId);
                        return userRepository.save(user);
                    }

                    return user; // user already exists
                }

                // Create new user
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUserName(name != null && !name.isEmpty() ? name : email); // Use name if available
                newUser.setProvider(AuthProvider.GOOGLE);
                newUser.setProviderId(googleId);

                // Fix: Set a placeholder encrypted password for OAuth users
                newUser.setPassword(passwordEncoder.encode("OAUTH_USER_" + googleId));

                Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
                newUser.getRoles().add(userRole);

                return userRepository.save(newUser);
            } else {
                throw new RuntimeException("Invalid ID token.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Token verification failed: " + e.getMessage(), e);
        }
    }
}