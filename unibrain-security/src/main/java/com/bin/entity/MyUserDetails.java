package com.bin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyUserDetails implements UserDetails {
    private User user;

    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities;

    public MyUserDetails(User user) {
        this.user = user;
        this.authorities = generateAuthorities(user.getRoles());
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.authorities != null && !this.authorities.isEmpty()) {
            return this.authorities;
        }
        return user != null ? generateAuthorities(user.getRoles()) : new ArrayList<>();
    }

    private Collection<? extends GrantedAuthority> generateAuthorities(List<Roles> roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roles == null || roles.isEmpty()) {
            return authorities; // 空角色返回空集合，避免循环空指针
        }
        for (Roles role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }


    @JsonIgnore
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
