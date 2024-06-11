package com.bsep.marketingacency.service;

import com.bsep.marketingacency.config.KeyStoreConfig;
import com.bsep.marketingacency.converter.AesEncryptor;
import com.bsep.marketingacency.dto.ClientDto;
import com.bsep.marketingacency.dto.UserDto;
import com.bsep.marketingacency.enumerations.RegistrationRequestStatus;
import com.bsep.marketingacency.model.Client;
import com.bsep.marketingacency.model.Package;
import com.bsep.marketingacency.model.Role;
import com.bsep.marketingacency.model.User;
import com.bsep.marketingacency.model.*;
import com.bsep.marketingacency.repository.AdvertisementRepository;
import com.bsep.marketingacency.repository.ClientRepository;
import com.bsep.marketingacency.repository.UserRepository;
import com.bsep.marketingacency.util.KeyGeneratorUtil;
import com.bsep.marketingacency.util.ThreadLocalClientId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.*;
import java.util.Date;
import java.util.List;


@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RejectionNoteService rejectionNoteService;

    @Autowired
    private ClientActivationTokenService clientActivationTokenService;

    @Autowired
    private PackageService packageService;

    private static final String CLIENT_ALIAS_PREFIX = "clientKey-";

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

//    public Client save(ClientDto clientDto) throws NoSuchAlgorithmException {
//
//        String mail = clientDto.getUser();
//        User user = userService.findByMail(mail);
//
//        String packageName = clientDto.getClientPackage();
//        Package pack = packageService.findByName(packageName);
//
//        Client client = new Client();
//        client.setUser(user);
//        client.setType(clientDto.getType());
//        client.setFirstName(clientDto.getFirstName());
//        client.setLastName(clientDto.getLastName());
//        client.setCompanyName(clientDto.getCompanyName());
//        client.setPib(clientDto.getPib());
//        client.setClientPackage(pack);
//        client.setPhoneNumber(clientDto.getPhoneNumber());
//        client.setAddress(clientDto.getAddress());
//        client.setCity(clientDto.getCity());
//        client.setCountry(clientDto.getCountry());
//        client.setIsApproved(RegistrationRequestStatus.PENDING);
//
//        Client savedClient = this.clientRepository.save(client);
//
//
//
//        SecretKey secretKey = generateAesKey();
//
//        try {
////            KeyStoreConfig keyStoreConfig = new KeyStoreConfig();
////            KeyStore keyStore = keyStoreConfig.keyStore();
//
//            KeyStore keyStore = KeyStore.getInstance("JKS");
//
//            KeyStore.ProtectionParameter protParam =
//                    new KeyStore.PasswordProtection("marketing-agency".toCharArray());
//
//            String alias = CLIENT_ALIAS_PREFIX + savedClient.getUser().getMail();
//
//            KeyStore.SecretKeyEntry skEntry =
//                    new KeyStore.SecretKeyEntry(secretKey);
//            keyStore.setEntry(alias, skEntry, protParam);
//
//
//            FileOutputStream fos = null;
//            try {
//                fos = new java.io.FileOutputStream("myKeystore" + CLIENT_ALIAS_PREFIX);
//                keyStore.store(fos, "marketing-agency".toCharArray());
//            } finally {
//                if (fos != null) {
//                    fos.close();
//                }
//            }
//
//        } catch (Exception e) {
//            logger.error("Error while storing AES key in keystore: " + e.getMessage());
//        }
//
//        return savedClient;
//
//    }

    public Client save(ClientDto clientDto) throws NoSuchAlgorithmException {

        String mail = clientDto.getUser();
        User user = userService.findByMail(mail);

        String packageName = clientDto.getClientPackage();
        Package pack = packageService.findByName(packageName);

        Client client = new Client();
        client.setUser(user);
        client.setType(clientDto.getType());
        client.setFirstName(clientDto.getFirstName());
        client.setLastName(clientDto.getLastName());
        client.setCompanyName(clientDto.getCompanyName());
        client.setPib(clientDto.getPib());
        client.setClientPackage(pack);
        client.setPhoneNumber(clientDto.getPhoneNumber());
        client.setAddress(clientDto.getAddress());
        client.setCity(clientDto.getCity());
        client.setCountry(clientDto.getCountry());
        client.setIsApproved(RegistrationRequestStatus.PENDING);

        Client savedClient = this.clientRepository.save(client);

        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            // Učitavanje postojećeg keystore-a
            FileInputStream fis = new FileInputStream("myKeystore.jks");
            keyStore.load(fis, "marketing-agency".toCharArray());
            fis.close();

            // Dodavanje novog ključa u keystore
            SecretKey secretKey = generateAesKey();
            String alias = CLIENT_ALIAS_PREFIX + savedClient.getUser().getMail();
            keyStore.setKeyEntry(alias, secretKey, "marketing-agency".toCharArray(), null);

            // Čuvanje izmenjenog keystore-a na disku
            FileOutputStream fos = new FileOutputStream("myKeystore.jks");
            keyStore.store(fos, "marketing-agency".toCharArray());
            fos.close();
        } catch (Exception e) {
            logger.error("Error while storing AES key in keystore: " + e.getMessage());
        }

        return savedClient;
    }


    private SecretKey generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    public void delete(Client client){
        this.clientRepository.delete(client);
    }

    public ClientActivationToken approveRegistrationRequest(Long id){
        Client client = clientRepository.getById(id);
        client.setIsApproved(RegistrationRequestStatus.APPROVED);
        this.clientRepository.save(client);
        ClientActivationToken token = new ClientActivationToken();
        token.setDuration(10);
        token.setUser(client.getUser());
        token.setCreationDate(new Date());
        token.setIsUsed(false);
        clientActivationTokenService.save(token);

        return token;
    }

    public void rejectRegistrationRequest(Long id, String reason){
        Client client = clientRepository.getById(id);
        client.setIsApproved(RegistrationRequestStatus.REJECTED);
        RejectionNote rejectionNote = new RejectionNote();
        rejectionNote.setEmail(client.getUser().getMail());
        rejectionNote.setRejectionDate(new Date());
        rejectionNote.setReason(reason);
        this.rejectionNoteService.save(rejectionNote);
    }

    public Client findById(Long id){
        return this.clientRepository.findById(id).orElse(null);
    }

    public Client findByUserId(Long id){
        return this.clientRepository.findByUserId(id);
    }

    public Boolean checkIfClientCanLoginWithoutPassword(String mail){
        User user = userService.findByMail(mail);
        Client client = clientRepository.findByUserId(user.getId());
        if (user == null || client == null || !user.getIsActivated()) {
            return false;
        }

        Package clientPackage = client.getClientPackage();

        if (clientPackage != null) {
            String packageName = clientPackage.getName();

            if ("GOLD".equals(packageName) || "STANDARD".equals(packageName)) {

                return true;
            }
        }
        return false;
    }
  
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientByUserId(Long userId) { return clientRepository.findByUserId(userId); }

    public Client updateClient(Client updatedClient) {
        Client existingClient = clientRepository.findById(updatedClient.getId())
                .orElse(null);
        if(existingClient != null) {
            existingClient.setFirstName(updatedClient.getFirstName());
            existingClient.setLastName(updatedClient.getLastName());
            existingClient.setAddress(updatedClient.getAddress());
            existingClient.setCity(updatedClient.getCity());
            existingClient.setCountry(updatedClient.getCountry());
            existingClient.setPhoneNumber(updatedClient.getPhoneNumber());

            return clientRepository.save(existingClient);
        } else {
            return null;
        }
    }

    public void deleteClient(Long userId) {
        Client client = clientRepository.findByUserId(userId);

        List<Advertisement> clientAdvertisements = advertisementRepository.findByClient(client);
        if (clientAdvertisements != null){
            advertisementRepository.deleteAll(clientAdvertisements);
        }

        if (client != null) {
            clientRepository.delete(client);
        }
    }
}
