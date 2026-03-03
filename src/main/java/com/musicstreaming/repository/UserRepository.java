package com.musicstreaming.repository;

import com.musicstreaming.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Для аутентификации загружаем только базовые данные, без коллекций
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // Убираем @EntityGraph для метода аутентификации
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    // Для поиска не загружаем коллекции
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> search(@Param("searchTerm") String searchTerm);

    // Для списка пользователей не загружаем коллекции
    @Query("SELECT u FROM User u ORDER BY u.role, u.username")
    List<User> findAllOrdered();

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Для профиля используем отдельный метод, который загружает все необходимое
    @EntityGraph(attributePaths = {"subscriptions"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithSubscriptions(@Param("id") Integer id);
}