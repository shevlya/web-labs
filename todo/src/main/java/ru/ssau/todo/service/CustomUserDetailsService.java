package ru.ssau.todo.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.todo.dto.UserDto;
import ru.ssau.todo.entity.Role;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.exception.RoleNotFoundException;
import ru.ssau.todo.exception.UsernameExistsException;
import ru.ssau.todo.repository.RoleRepository;
import ru.ssau.todo.repository.UserRepository;
import ru.ssau.todo.security.CustomUserDetails;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден"));
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();
        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }

    @Transactional
    public UserDto register(UserDto userDto) throws UsernameExistsException, RoleNotFoundException {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UsernameExistsException();
        }
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        String roleName = userDto.getUsername().equalsIgnoreCase("admin") ? "ROLE_ADMIN" : "ROLE_USER";
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new RoleNotFoundException(roleName));
        user.getRoles().add(role);
        User savedUser = userRepository.save(user);
        return new UserDto(savedUser.getId(), savedUser.getUsername());
    }
}