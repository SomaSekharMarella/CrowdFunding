package com.crowdfunding.config;

import com.crowdfunding.entity.Role;
import com.crowdfunding.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize default roles on application startup
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    
    @Override
    public void run(String... args) {
        // Create default roles if they don't exist
        if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USER);
            roleRepository.save(userRole);
        }
        
        if (roleRepository.findByName(Role.RoleName.ROLE_CREATOR).isEmpty()) {
            Role creatorRole = new Role();
            creatorRole.setName(Role.RoleName.ROLE_CREATOR);
            roleRepository.save(creatorRole);
        }
        
        if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }
    }
}
