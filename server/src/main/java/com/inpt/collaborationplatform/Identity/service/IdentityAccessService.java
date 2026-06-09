package com.inpt.collaborationplatform.Identity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.inpt.collaborationplatform.Identity.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class IdentityAccessService {

    private final UserRepository userRepository;

    public void requireUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
