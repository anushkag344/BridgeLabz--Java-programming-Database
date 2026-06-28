package greet.repository;

import greet.model.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class GreetingRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Greeting> mapper =
            (rs, rowNum) -> new Greeting(
                    rs.getInt("id"),
                    rs.getString("message"),
                    rs.getInt("created_by"),
                    rs.getString("creator")
            );

    public List<Greeting> findAll() {

        String sql =
                "select g.id,g.message,g.created_by,u.username as creator " +
                        "from greetings g " +
                        "left join users u on g.created_by=u.id";

        return jdbcTemplate.query(sql, mapper);
    }

    public void save(String message, int createdBy) {

        String sql =
                "insert into greetings(message,created_by) values(?,?)";

        jdbcTemplate.update(sql, message, createdBy);
    }

    public int update(int id, String msg) {

        String sql =
                "update greetings set message=? where id=?";

        return jdbcTemplate.update(sql, msg, id);
    }

    public int delete(int id) {

        String sql =
                "delete from greetings where id=?";

        return jdbcTemplate.update(sql, id);
    }

    public int getGreetingCountForUser(String username) {

        String sql =
                "select get_user_greeting_count(?)";

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                username
        );
    }

    public List<Map<String,Object>> getAuditLogs() {

        return jdbcTemplate.queryForList(
                "select * from greeting_audit order by changed_at desc"
        );
    }
}