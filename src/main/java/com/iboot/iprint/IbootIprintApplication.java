package com.iboot.iprint;

import com.iboot.iprint.entity.User;
import com.iboot.iprint.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
public class IbootIprintApplication {

    public static void main(String[] args) {
        SpringApplication.run(IbootIprintApplication.class, args);
    }

    /**
     * 初始化默认管理员账户（仅在数据库中不存在时创建）
     */
    @Bean
    CommandLineRunner initDefaultAdmin(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("123456"))
                        .build();
                userRepository.save(admin);
                log.info("默认管理员账户已创建: admin/123456");
            }
        };
    }
}
