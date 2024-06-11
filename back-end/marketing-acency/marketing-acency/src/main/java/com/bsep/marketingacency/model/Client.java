package com.bsep.marketingacency.model;

import com.bsep.marketingacency.converter.AesEncryptor;
import com.bsep.marketingacency.enumerations.ClientType;
import com.bsep.marketingacency.enumerations.RegistrationRequestStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.beans.ConstructorProperties;

@Getter
@Setter
@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private ClientType type;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "pib")
    private Integer pib;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private Package clientPackage;

    @Column(name = "phone_number")
    //@Convert(converter = AesEncryptor.class)
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "is_approved")
    private RegistrationRequestStatus isApproved;


    public Client() {
    }

    public Client(Long id, User user, ClientType type, String firstName, String lastName, String companyName, Integer pib, Package clientPackage, String phoneNumber, String address, String city, String country, RegistrationRequestStatus isApproved) {
        Id = id;
        this.user = user;
        this.type = type;
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
        this.pib = pib;
        this.clientPackage = clientPackage;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.country = country;
        this.isApproved = isApproved;
    }

    public Long getId() {
        return Id;
    }

    public User getUser() {
        return user;
    }

    public ClientType getType() {
        return type;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Integer getPib() {
        return pib;
    }

    public Package getClientPackage() {
        return clientPackage;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public RegistrationRequestStatus getIsApproved() {
        return isApproved;
    }

    public void setId(Long id) {
        Id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setType(ClientType type) {
        this.type = type;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setPib(Integer pib) {
        this.pib = pib;
    }

    public void setClientPackage(Package clientPackage) {
        this.clientPackage = clientPackage;
    }



    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setIsApproved(RegistrationRequestStatus isApproved) {
        this.isApproved = isApproved;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
