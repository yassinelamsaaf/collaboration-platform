package com.inpt.collaborationplatform.Identity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.inpt.collaborationplatform.Identity.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdentityAccessService {

    private final UserRepository userRepository;

    public void requireUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    public String requireUserEmail(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .getEmail();
    }

    public Optional<String> findUserIdByEmail(String email) {
        return userRepository.findByEmail(email).map((user) -> user.getId());
    }
}
