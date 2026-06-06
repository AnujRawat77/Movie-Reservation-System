package com.movie_reservation.MovieReservationSystem.security;

import com.movie_reservation.MovieReservationSystem.constant.UserRole;
import com.movie_reservation.MovieReservationSystem.entity.User;
import com.movie_reservation.MovieReservationSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String role = UserRole.ADMIN.equals(user.getRole()) ? UserRole.ROLE_ADMIN : UserRole.ROLE_USER;

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(user.getActive()))
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
    }
}
