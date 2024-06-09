package com.bsep.marketingacency.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String mail;

    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles;

    @Column(name = "is_activated")
    private Boolean isActivated;

    @Column(name = "is_blocked")
    private Boolean isBlocked;

    @Nullable
    @Column(name = "mfa")
    private boolean mfa;

    @Nullable
    @Column(name = "secret")
    private String secret;

    public String getRoleName() {
        return roles.get(0).getName();
    }

    public Role getRole(){
        return roles.get(0);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Permission> permissions = new HashSet<Permission>();
        for(Role role : this.roles){
            for(Permission permission : role.getPermissions()){
                permissions.add(permission);
            }
        }
        return permissions;
    }

    public User() {
    }

    public User(Long id, String mail, String password, List<Role> roles, Boolean isActivated, Boolean isBlocked, @Nullable boolean mfa, @Nullable String secret) {
        this.id = id;
        this.mail = mail;
        this.password = password;
        this.roles = roles;
        this.isActivated = isActivated;
        this.isBlocked = isBlocked;
        this.mfa = mfa;
        this.secret = secret;
    }

    @Override
    public String getUsername() {
        return mail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
