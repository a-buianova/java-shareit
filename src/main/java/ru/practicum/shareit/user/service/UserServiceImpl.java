package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exception.ConflictException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.InMemoryUserRepository;

import java.util.List;

/**
 * @apiNote Default implementation with email uniqueness guarantees.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final InMemoryUserRepository repo;
    private final UserMapper mapper;

    @Override
    public UserResponse create(UserCreateDto dto) {
        if (repo.existsByEmail(dto.email())) {
            throw new ConflictException("email already exists");
        }
        User saved = repo.save(mapper.toEntity(dto));
        return mapper.toResponse(saved);
    }

    @Override
    public UserResponse get(Long id) {
        User user = repo.findById(id).orElseThrow(() -> new NotFoundException("user not found"));
        return mapper.toResponse(user);
    }

    @Override
    public List<UserResponse> list() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public UserResponse patch(Long id, UserUpdateDto dto) {
        User existing = repo.findById(id).orElseThrow(() -> new NotFoundException("user not found"));
        String oldEmail = existing.getEmail();
        mapper.patch(existing, dto);

        if (dto.email() != null && !dto.email().equalsIgnoreCase(oldEmail)) {
            if (repo.existsByEmail(dto.email())) {
                throw new ConflictException("email already exists");
            }
            repo.reindexEmail(oldEmail, dto.email(), existing.getId());
        }

        User updated = repo.update(existing).orElseThrow(() -> new NotFoundException("user not found"));
        return mapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        boolean removed = repo.delete(id);
        if (!removed) {
            throw new NotFoundException("user not found");
        }
    }
}