package com.marketplacem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplacem.entity.User;
import com.marketplacem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class Auth0Service {

    private static final Logger logger = LoggerFactory.getLogger(Auth0Service.class);

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.clientId}")
    private String clientId;

    @Value("${auth0.clientSecret}")
    private String clientSecret;

    @Value("${auth0.audience}")
    private String audience;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public Auth0Service(RestTemplate restTemplate, UserRepository userRepository, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public String generateOneTimeToken(String username) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "password");
            map.add("username", username);
            map.add("password", "PLACEHOLDER_PASSWORD"); // In a real scenario, you'd use a real password
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("audience", audience);
            map.add("scope", "openid profile email");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://" + auth0Domain + "/oauth/token",
                    request,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String oneTimeToken = root.path("access_token").asText();

            // Log the one-time token for demonstration purposes
            logger.info("Generated one-time token for user {}: {}", username, oneTimeToken);

            return oneTimeToken;
        } catch (Exception e) {
            logger.error("Error generating one-time token", e);
            throw new RuntimeException("Failed to generate one-time token", e);
        }
    }

    public User getUserInfoFromAuth0(String oneTimeToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(oneTimeToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://" + auth0Domain + "/userinfo",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode userInfo = objectMapper.readTree(response.getBody());

            String email = userInfo.path("email").asText();
            String externalId = userInfo.path("sub").asText();

            Optional<User> existingUser = userRepository.findByExternalId(externalId);

            if (existingUser.isPresent()) {
                return existingUser.get();
            } else {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setExternalId(externalId);
                newUser.setFirstName(userInfo.path("given_name").asText(""));
                newUser.setLastName(userInfo.path("family_name").asText(""));
                newUser.setActive(true);

                return userRepository.save(newUser);
            }
        } catch (Exception e) {
            logger.error("Error getting user info from Auth0", e);
            throw new RuntimeException("Failed to get user info from Auth0", e);
        }
    }
}
