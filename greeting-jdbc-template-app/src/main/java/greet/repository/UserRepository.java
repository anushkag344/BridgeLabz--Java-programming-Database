package greet.repository;

import greet.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> mapper =
            (rs, rowNum) -> new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("role")
            );

    public void save(User user) {

        String sql =
                "insert into users(username,password,email,role) values(?,?,?,?)";

        jdbcTemplate.update(
                sql,
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole()
        );
    }

    public User findByUsername(String username) {

        String sql =
                "select * from users where username=?";

        try {
            return jdbcTemplate.queryForObject(
                    sql,
                    mapper,
                    username
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}