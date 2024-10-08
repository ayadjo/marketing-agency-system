package com.bsep.marketingacency.controller;

import com.bsep.marketingacency.TokenRefreshException;
import com.bsep.marketingacency.dto.*;
import com.bsep.marketingacency.model.*;
import com.bsep.marketingacency.enumerations.RegistrationRequestStatus;
import com.bsep.marketingacency.service.*;
import com.bsep.marketingacency.util.HashUtil;
import com.bsep.marketingacency.util.TokenUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "https://localhost:4200")
public class AuthenticationController {
   // @Value("6LcDAe8pAAAAALU_7vCCHftQh0wgcjYOOPKLifoI")
  //  private String recaptchaSecret;

  //  @Value("https://www.google.com/recaptcha/api/siteverify")
  //  private String recaptchaServerUrl;
//    @Bean
 //   public RestTemplate restTemplate(RestTemplateBuilder builder) {
 //       return builder.build();
  //  }

//    @Autowired
  //  private RestTemplate restTemplate;
    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private RecaptchaService recaptchaService;

    private final static Logger logger = LogManager.getLogger(AuthenticationController.class);



//    @PostMapping(value = "/login")
//    public ResponseEntity<?> createAuthenticationToken(
//            @RequestBody JwtAuthenticationRequest authenticationRequest,
//            HttpServletResponse response
//    ) {
//        User user = userService.findByMail(authenticationRequest.getMail());
//        if (user == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//        }
//
//        if (user.getRole().getName().equals("ROLE_EMPLOYEE")) {
//            String gRecaptchaResposnse = authenticationRequest.getCaptchaResponse();
//            try {
//                verifyRecaptcha(gRecaptchaResposnse);
//            } catch (Exception ex) {
//                logger.error("Error verifying ReCaptcha for user with email {}.", authenticationRequest.getMail(), ex);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying ReCaptcha");
//            }
//
//        }
//
//        try {
//            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                    authenticationRequest.getMail(), authenticationRequest.getPassword()));
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            User authenticatedUser = (User) authentication.getPrincipal();
//
//
//            if (authenticatedUser.isMfa()) {
//                logger.info("User with email {} is using two-factor authentication.", authenticatedUser.getMail());
//                return ResponseEntity.ok().body("");
//            }
//
//            String jwt = tokenUtils.generateToken(authenticatedUser);
//            int expiresIn = tokenUtils.getExpiredIn();
//
//            String refreshJwt = tokenUtils.generateRefreshToken(authenticatedUser);
//            int refreshExpiresIn = tokenUtils.getRefreshExpiredIn();
//
//            UserTokenState tokenState = new UserTokenState(jwt, expiresIn, refreshJwt, refreshExpiresIn);
//            logger.info("User with email {} successfully logged in.", authenticatedUser.getMail());
//            return ResponseEntity.ok(tokenState);
//        } catch (BadCredentialsException ex) {
//            logger.warn("Invalid credentials for user with email {}.", authenticationRequest.getMail());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//        } catch (AuthenticationException ex) {
//        logger.error("Authentication failed for user with email {}.", authenticationRequest.getMail(), ex);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication failed");
//    }
//    }

    private String getUserIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            String remoteAddr = request.getRemoteAddr();
            logger.debug("RemoteAddr: {}", remoteAddr);
            return remoteAddr;
        }
        String[] ips = xForwardedForHeader.split(",");
        String userIpAddress = ips[0].trim();
        return userIpAddress;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody JwtAuthenticationRequest authenticationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String userIpAddress = getUserIpAddress(request);
        logger.info("Someone is trying to login from IP address: {}.", userIpAddress);
        try {
            User user = userService.findByMail(authenticationRequest.getMail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            if (user.getIsBlocked()) {
                logger.warn("User {} is blocked.", user.getMail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is blocked.");
            }
            if (!user.getIsActivated()) {
                logger.warn("User {} is not activated.", user.getMail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not activated.");
            }

            if (user.getRole().getName().equals("ROLE_EMPLOYEE")) {
                String gRecaptchaResponse = authenticationRequest.getCaptchaResponse();
                try {
                    recaptchaService.verifyRecaptcha(gRecaptchaResponse);
                } catch (Exception ex) {
                    logger.error("Error verifying ReCaptcha for user {}.", authenticationRequest.getMail(), ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying ReCaptcha.");
                }
            }

            try {
                Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getMail(), authenticationRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                User authenticatedUser = (User) authentication.getPrincipal();

                if (authenticatedUser.isMfa()) {
                    logger.info("User {} is using two-factor authentication.", authenticatedUser.getMail());
                    return ResponseEntity.ok().body("");
                }

                String jwt = tokenUtils.generateToken(authenticatedUser);
                int expiresIn = tokenUtils.getExpiredIn();

                String refreshJwt = tokenUtils.generateRefreshToken(authenticatedUser);
                int refreshExpiresIn = tokenUtils.getRefreshExpiredIn();

                UserTokenState tokenState = new UserTokenState(jwt, expiresIn, refreshJwt, refreshExpiresIn);
                logger.info("User {} successfully logged in.", authenticatedUser.getMail());
                return ResponseEntity.ok(tokenState);
            } catch (BadCredentialsException ex) {
                logger.warn("Invalid credentials for {}.", authenticationRequest.getMail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }

        } catch (AuthenticationException ex) {
            logger.error("Authentication failed for user {}.", authenticationRequest.getMail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication failed");
        }
    }

/*
    private void verifyRecaptcha(String gRecaptchaResposnse){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secret", recaptchaSecret);
        map.add("response", gRecaptchaResposnse);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        RecaptchaResponse response = restTemplate.postForObject(recaptchaServerUrl,request, RecaptchaResponse.class);

        logger.info("Verifying recaptcha, Success: {}", response.isSuccess());

        if(response.getErrorCodes() != null){
            for(String error : response.getErrorCodes()){
                System.out.println("\t" + error);
            }
        }


    }*/

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyTOTP verifyTOTP) {
        logger.info("Trying to verify 2fa code for {}.", verifyTOTP.getMail());
        try {
            Boolean isValid = userService.verify(verifyTOTP.getMail(), verifyTOTP.getCode());

            if (isValid) {
                User user = userService.findByMail(verifyTOTP.getMail());

                String jwt = tokenUtils.generateToken(user);
                int expiresIn = tokenUtils.getExpiredIn();

                String refreshJwt = tokenUtils.generateRefreshToken(user);
                int refreshExpiresIn = tokenUtils.getRefreshExpiredIn();

                UserTokenState tokenState = new UserTokenState(jwt, expiresIn, refreshJwt, refreshExpiresIn);
                logger.info("User {} successfully passed 2fA and logged in.", verifyTOTP.getMail());
                return ResponseEntity.ok(tokenState);
            } else {
                logger.warn("Invalid 2fA code for {}.", verifyTOTP.getMail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid verification code.");
            }
        } catch (Exception ex) {
            logger.error("Error during 2FA verification for {}.", verifyTOTP.getMail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during 2FA verification.");
        }
    }



    @PostMapping(value = "/passwordless-login")
    public ResponseEntity<String> sendLoginToken(
            @RequestBody String mail) throws InterruptedException {
        if(!clientService.checkIfClientCanLoginWithoutPassword(mail)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        emailService.sendLoginToken(mail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    @PostMapping(value = "/login-tokens")
//    public ResponseEntity<UserTokenState> createAuthenticationTokenWithoutPassword(
//            @RequestBody String mail) {
//
//        User user = userService.findByMail(mail);
//
//        String jwt = tokenUtils.generateToken(user);
//        int expiresIn = tokenUtils.getExpiredIn();
//
//        String refresh_jwt = tokenUtils.generateRefreshToken(user);
//        int refreshExpiresIn = tokenUtils.getRefreshExpiredIn();
//
//        if (!user.getIsBlocked()) {
//            return ResponseEntity.ok(new UserTokenState(jwt, expiresIn, refresh_jwt, refreshExpiresIn));
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//
//
//    }

    @PostMapping(value = "/login-tokens")
    public ResponseEntity<UserTokenState> createAuthenticationTokenWithoutPassword(
            @RequestBody String mail) {

        logger.info("Attempting to generate jwt tokens without password for client {}.", mail );
        User user = userService.findByMail(mail);

        if (user != null) {
            if (!user.getIsBlocked() && user.getIsActivated()) {
                String jwt = tokenUtils.generateToken(user);
                int expiresIn = tokenUtils.getExpiredIn();

                String refresh_jwt = tokenUtils.generateRefreshToken(user);
                int refreshExpiresIn = tokenUtils.getRefreshExpiredIn();

                logger.info("Client {} successfully logged in without password.", mail);

                return ResponseEntity.ok(new UserTokenState(jwt, expiresIn, refresh_jwt, refreshExpiresIn));
            } else {
                if (user.getIsBlocked()) {
                    logger.warn("Passwordless login failed, client {} is blocked.", mail);
                } else {
                    logger.warn("Passwordless login failed, client {} is not activated..", mail);
                }
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }



    @GetMapping("/{tokenId}")
    public ResponseEntity<User> findUser(@PathVariable("tokenId") UUID tokenId) {
        try {
            logger.info("Trying to check passwordless login token {} validity.", HashUtil.hash(tokenId.toString()));
            User user = loginTokenService.findUser(tokenId);
            if (user != null) {
                Boolean isTokenUsed = loginTokenService.checkIfUsed(tokenId);
                if (!isTokenUsed) {
                    logger.info("Passwordless login token {} is valid.", HashUtil.hash(tokenId.toString()));
                    return ResponseEntity.ok(user);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing passwordless login.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }





    // pristup: svi
    @GetMapping(value = "/findByEmail/{mail}")
    public ResponseEntity<User> findByMail(@PathVariable String mail){
        User user = userService.findByMail(mail);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    @PostMapping("/refreshToken")
//    public ResponseEntity<NewAccessToken> refreshtoken(@RequestBody String mail) {
//        User user = userService.findByMail(mail);
//        String jwt = tokenUtils.generateToken(user);
//        int expiresIn = tokenUtils.getExpiredIn();
//
//        NewAccessToken newAccessToken = new NewAccessToken(jwt, expiresIn);
//        if (!user.getIsBlocked()) {
//            return new ResponseEntity<>(newAccessToken, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }

    @PostMapping("/refreshToken")
    public ResponseEntity<NewAccessToken> refreshToken(@RequestBody String mail) {
        User user = userService.findByMail(mail);
        logger.info("Trying to refresh token for {}.", mail);
        if (user != null) {
            String jwt = tokenUtils.generateToken(user);
            int expiresIn = tokenUtils.getExpiredIn();
            NewAccessToken newAccessToken = new NewAccessToken(jwt, expiresIn);
            if (!user.getIsBlocked()) {
                logger.info("Token refreshed successfully for user {}.", mail);
                return new ResponseEntity<>(newAccessToken, HttpStatus.OK);
            } else {
                logger.warn("Token refresh failed, user {} is blocked.", mail);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            logger.warn("Token refresh failed, user {} not found.", mail);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @GetMapping(value = "/findByUserId/{id}")
//    public ResponseEntity<User> findById(@PathVariable Long id) {
//        User user = userService.findUserById(id);
//        if (user != null) {
//            return ResponseEntity.ok(user);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

    @GetMapping(value = "/findByUserId/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        try {
            User user = userService.findUserById(id);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error occurred while finding user by ID: {}.", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


//    @GetMapping(value = "/allIndividuals")
//    @PreAuthorize("hasAuthority('VIEW_ALL_INDIVIDUALS')")
//    public ResponseEntity<List<Client>> getAllIndividuals() {
//        List<Client> individualClients = userService.getAllIndividuals();
//
//        List<Client> filteredClients = individualClients.stream()
//                .filter(client -> client.getIsApproved() == RegistrationRequestStatus.PENDING)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(filteredClients);
//    }

    @GetMapping(value = "/allIndividuals")
    @PreAuthorize("hasAuthority('VIEW_ALL_INDIVIDUALS')")
    public ResponseEntity<List<Client>> getAllIndividuals() {
        logger.info("Trying to retrieve registration requests form individual clients.");
        try {
            List<Client> individualClients = userService.getAllIndividuals();

            List<Client> filteredClients = individualClients.stream()
                    .filter(client -> client.getIsApproved() == RegistrationRequestStatus.PENDING)
                    .collect(Collectors.toList());

            if(filteredClients.isEmpty()){
                logger.info("No requests for registration from individual clients found.");
            }else{
                logger.info("Requests for registration form individual clients successfully found.");
            }

            return ResponseEntity.ok(filteredClients);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving registration requests form individual clients.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


//    @GetMapping(value = "/allLegalEntities")
//    @PreAuthorize("hasAuthority('VIEW_ALL_LEGAL_ENTITIES')")
//    public ResponseEntity<List<Client>> getAllLegalEntities() {
//        List<Client> legalEntityClients = userService.getAllLegalEntities();
//
//        List<Client> filteredClients = legalEntityClients.stream()
//                .filter(client -> client.getIsApproved() == RegistrationRequestStatus.PENDING)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(filteredClients);
//    }

    @GetMapping(value = "/allLegalEntities")
    @PreAuthorize("hasAuthority('VIEW_ALL_LEGAL_ENTITIES')")
    public ResponseEntity<List<Client>> getAllLegalEntities() {
        logger.info("Trying to retrieve registration requests from legal entity clients.");
        try {
            List<Client> legalEntityClients = userService.getAllLegalEntities();

            List<Client> filteredClients = legalEntityClients.stream()
                    .filter(client -> client.getIsApproved() == RegistrationRequestStatus.PENDING)
                    .collect(Collectors.toList());

            if(filteredClients.isEmpty()){
                logger.info("No requests for registration from legal entity clients found.");
            }else{
                logger.info("Requests for registration form legal entity clients successfully found.");
            }

            return ResponseEntity.ok(filteredClients);

        } catch (Exception ex) {
            logger.error("An error occurred while fetching legal entity client registration requests.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


//    @PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//        userService.sendPasswordResetLink(email);
//        return ResponseEntity.ok("Password reset link sent");
//    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("User {} wants to reset password.", email);
        try {
            userService.sendPasswordResetLink(email);
            return ResponseEntity.ok("Password reset link sent");
        } catch (Exception ex) {
            logger.error("Error while sending password reset link to {}.", email);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while sending password reset link");
        }
    }


//    @PostMapping("/change-password")
//    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> request) {
//        String token = request.get("token");
//        String newPassword = request.get("password");
//        userService.changePassword(token, newPassword);
//        return ResponseEntity.ok("Password changed successfully");
//    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");
        logger.info("Password resetting for reset token {}.", HashUtil.hash(token));
        try {
            userService.changePassword(token, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception ex) {
            logger.error("Error while changing password for {}.", token);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while changing password");
        }
    }

}
