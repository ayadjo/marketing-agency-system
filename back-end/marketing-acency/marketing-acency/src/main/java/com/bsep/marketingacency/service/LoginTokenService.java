package com.bsep.marketingacency.service;

import com.bsep.marketingacency.model.Client;
import com.bsep.marketingacency.model.ClientActivationToken;
import com.bsep.marketingacency.model.LoginToken;
import com.bsep.marketingacency.model.User;
import com.bsep.marketingacency.repository.LoginTokenRepository;
import com.bsep.marketingacency.util.HashUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class LoginTokenService {
    @Autowired
    private LoginTokenRepository loginTokenRepository;

    @Autowired
    private ClientService clientService;

    private final static Logger logger = LogManager.getLogger(LoginTokenService.class);

    public LoginToken save(LoginToken token){
        return loginTokenRepository.save(token);
    }

    public User findUser(UUID tokenId){
        LoginToken token = loginTokenRepository.findById(tokenId).orElse(null);
        if (token == null) {
            logger.warn("No passwordless login token found for token ID {}.", HashUtil.hash(String.valueOf(tokenId)));
            return null;
        }
        if (isExpired(token)) {
            logger.warn("Passwordless login token {} has expired.", HashUtil.hash(String.valueOf(tokenId)));
            return null;
        }
        User user = token.getUser();
        return user;
    }

    public boolean isExpired(LoginToken token){
        Date currentTime = new Date();
        Date expirationTime = new Date(token.getCreationDate().getTime() + token.getDuration() * 60 * 1000);
        return currentTime.after(expirationTime);
    }

//    public Boolean checkIfUsed(UUID tokenId){
//        LoginToken token = loginTokenRepository.findById(tokenId).orElseGet(null);
//        if(!token.getIsUsed()){
//            token.setIsUsed(true);
//            save(token);
//            return false;
//        }
//        return  true;
//    }

    public Boolean checkIfUsed(UUID tokenId) {
        //logger.info("Checking if login token with ID {} has been used.", HashUtil.hash(tokenId.toString()));

        LoginToken token = loginTokenRepository.findById(tokenId).orElse(null);

        if (!token.getIsUsed()) {
            token.setIsUsed(true);
            save(token);
            return false;
        } else {
            logger.info("Passwordless login token {} has already been used.", HashUtil.hash(tokenId.toString()));
        }
        return true;
    }

//    public void delete(Long userId) {
//        loginTokenRepository.deleteAllByUserId(userId);
//    }

    public void delete(Long userId) {
        try {
            loginTokenRepository.deleteAllByUserId(userId);
            //logger.info("Deleted all login tokens for user with userId {}", userId);
        } catch (Exception e) {
            logger.error("Error while deleting login tokens for user {}.", HashUtil.hash(userId.toString()));
        }
    }

}
