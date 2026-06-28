package payroll.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import payroll.model.User;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {

        User user = new User();

        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));

        return user;
    };

    /**
     * Find user by username.
     */
    public User findByUsername(String username) {

        String sql = """
                SELECT *
                FROM users
                WHERE username = ?
                """;

        try {

            return jdbcTemplate.queryForObject(
                    sql,
                    userRowMapper,
                    username
            );

        } catch (EmptyResultDataAccessException ex) {

            return null;

        }

    }

    /**
     * Find user by email.
     */
    public User findByEmail(String email) {

        String sql = """
                SELECT *
                FROM users
                WHERE email = ?
                """;

        try {

            return jdbcTemplate.queryForObject(
                    sql,
                    userRowMapper,
                    email
            );

        } catch (EmptyResultDataAccessException ex) {

            return null;

        }

    }

    /**
     * Register new user.
     */
    public void registerUser(String username,
                             String hashedPassword,
                             String email,
                             String role) {

        String sql = """
                INSERT INTO users
                (
                    username,
                    password,
                    email,
                    role
                )
                VALUES
                (
                    ?, ?, ?, ?
                )
                """;

        jdbcTemplate.update(
                sql,
                username,
                hashedPassword,
                email,
                role
        );

    }

    /**
     * Check if username already exists.
     */
    public boolean usernameExists(String username) {

        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE username = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                username
        );

        return count != null && count > 0;

    }

    /**
     * Check if email already exists.
     */
    public boolean emailExists(String email) {

        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE email = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                email
        );

        return count != null && count > 0;

    }

    /**
     * Get user by id.
     */
    public User findById(int id) {

        String sql = """
                SELECT *
                FROM users
                WHERE id = ?
                """;

        try {

            return jdbcTemplate.queryForObject(
                    sql,
                    userRowMapper,
                    id
            );

        } catch (EmptyResultDataAccessException ex) {

            return null;

        }

    }

}