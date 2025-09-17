package ru.practicum.shareit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.item.repo.InMemoryItemRepository;
import ru.practicum.shareit.user.repo.InMemoryUserRepository;

/**
 * @apiNote Central application configuration for in-memory repositories (sprint 14 only).
 */
@Configuration
public class AppConfig {

    @Bean
    public InMemoryUserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    public InMemoryItemRepository itemRepository() {
        return new InMemoryItemRepository();
    }
}