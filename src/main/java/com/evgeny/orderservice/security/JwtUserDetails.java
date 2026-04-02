package com.evgeny.orderservice.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class JwtUserDetails implements UserDetails {

    private Long id;
    private String role;

    public JwtUserDetails(Long id, String role) {
        this.id = id;
        this.role = role;
    }

    public Long getId() { return id; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + role);
    }


    @Override
    public String getPassword() { return null; }
    @Override
    public String getUsername() { return id.toString(); }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
