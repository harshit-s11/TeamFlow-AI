package com.teamflow.backend.repository;

import com.teamflow.backend.domain.model.User;
import com.teamflow.backend.domain.model.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindById_returnsPersistedUser() {
        User user = User.create("Alice Smith", "alice@teamflow.com");
        User saved = userRepository.save(user);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("Alice Smith");

        Optional<User> found = userRepository.findById(saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().email()).isEqualTo("alice@teamflow.com");
    }

    @Test
    void save_withDuplicateEmail_throwsDataIntegrityViolation() {
        User user1 = User.create("Bob", "bob@teamflow.com");
        userRepository.save(user1);

        User user2 = User.create("Bob Duplicate", "bob@teamflow.com");
        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAll_returnsAllUsers() {
        userRepository.save(User.create("User 1", "u1@teamflow.com"));
        userRepository.save(User.create("User 2", "u2@teamflow.com"));

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void findAccountByEmail_whenUserExists_returnsUserAccount() {
        User user = User.create("Carol", "carol@teamflow.com");
        User saved = userRepository.save(user);

        Optional<UserAccount> accountOptional = userRepository.findAccountByEmail(saved.email());

        assertThat(accountOptional).isPresent();
        UserAccount account = accountOptional.get();
        assertThat(account.id()).isEqualTo(saved.id());
        assertThat(account.email()).isEqualTo("carol@teamflow.com");
        assertThat(account.role()).isEqualTo("USER");
        assertThat(account.passwordHash()).isNull();
    }

    @Test
    void findAccountByEmail_whenUserDoesNotExist_returnsEmptyOptional() {
        Optional<UserAccount> accountOptional = userRepository.findAccountByEmail("nonexistent@teamflow.com");

        assertThat(accountOptional).isEmpty();
    }
}
