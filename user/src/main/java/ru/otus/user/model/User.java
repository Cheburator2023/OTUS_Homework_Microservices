package ru.otus.user.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    // Конструктор для регистрации (с паролем)
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Конструктор без пароля (для обратной совместимости)
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.password = "defaultPassword123"; // или генерируем случайный
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return getId().equals(user.getId()) && getName().equals(user.getName());
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                " name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}