package com.project.recommendation_engine.model;

import com.project.recommendation_engine.config.AppConfig;
import com.project.recommendation_engine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@Import({AppConfig.class})
class UserTest {

    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder encoder;

    private User sampleUser;

    @BeforeEach
    void setUp(){
        sampleUser = new User();
        sampleUser.setUsername("filmlover");
        sampleUser.setEmail("filmlover@example.com");
        sampleUser.setPassword("secret");
    }

    @Test
    @DisplayName("All getter/setter should preserve values")
    void gettersAndSetters(){
        User u = new User();
        u.setUsername("john");
        u.setEmail("john@doe.com");
        u.setPassword("pass");

        assertThat(u.getUsername()).isEqualTo("john");
        assertThat(u.getEmail()).isEqualTo("john@doe.com");
        assertThat(u.getPassword()).isEqualTo("pass");
    }

    @Nested
    @DisplayName("Persisting and loading a User")
    class Persisting{

        @Test
        void shouldPersistUserAndReturnId(){
            User persisted = userRepository.save(sampleUser);
            assertThat(persisted.getId()).isNotNull();

            Optional<User> fromDb = userRepository.findById(persisted.getId());
            assertThat(fromDb).isPresent();
            assertThat(fromDb.get().getEmail()).isEqualTo(sampleUser.getEmail());
        }

        @Test
        void shouldNotPersistDuplicateEmail(){
            User u1 = new User();
            u1.setUsername("user1");
            u1.setEmail("dup@example.com");
            u1.setPassword("pw1");

            User u2 = new User();
            u2.setUsername("user2");
            u2.setEmail("dup@example.com");
            u2.setPassword("pw2");

            userRepository.save(u1);
            // The second insert will throw DuplicateKeyException at commit time
            assertThatThrownBy(() -> userRepository.save(u2)).isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
        }

        @Test
        void createdAtShouldBeSetWhenPersisted(){
            User persisted = userRepository.save(sampleUser);
            assertThat(persisted.getCreatedAt()).isNotNull();
            assertThat(persisted.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }

    @Test
    @DisplayName("Password should be stored encrypted")
    void passwordEncryption(){
        User persisted = userRepository.save(sampleUser);

        // The encoder used by the application (via AppConfig) should match
        assertThat(encoder.matches("secret", persisted.getPassword())).isTrue();
    }
}