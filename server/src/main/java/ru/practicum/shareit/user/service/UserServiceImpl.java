package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exception.ConflictException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserResponse create(UserCreateDto dto) {
        if (repo.existsByEmailIgnoreCase(dto.email())) {
            throw new ConflictException("Email already exists: " + dto.email());
        }
        User entity = mapper.toEntity(dto);
        User saved = repo.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public UserResponse get(Long id) {
        return repo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Override
    public List<UserResponse> list() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse patch(Long id, UserUpdateDto dto) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (dto.name() != null && !dto.name().isBlank()) {
            existing.setName(dto.name());
        }

        if (dto.email() != null && !dto.email().equalsIgnoreCase(existing.getEmail())) {
            if (repo.existsByEmailIgnoreCase(dto.email())) {
                throw new ConflictException("Email already exists: " + dto.email());
            }
            existing.setEmail(dto.email());
        }

        User saved = repo.save(existing);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }
        repo.deleteById(id);
    }
}