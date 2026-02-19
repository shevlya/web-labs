package ru.ssau.todo.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.exception.TaskNotFoundException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Profile("jdbc")
public class TaskJdbcRepository implements TaskRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public TaskJdbcRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    private final RowMapper<Task> taskRowMapper = (rs, rowNum) -> {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setCreatedBy(rs.getLong("created_by"));
        Timestamp ts = rs.getTimestamp("created_at");
        task.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return task;
    };

    @Override
    public Task create(Task task) {
        String sql = "INSERT INTO task (title, status, created_by, created_at) VALUES (:title, :status, :createdBy, :createdAt)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", task.getTitle())
                .addValue("status", task.getStatus().name())
                .addValue("createdBy", task.getCreatedBy())
                .addValue("createdAt", Timestamp.valueOf(task.getCreatedAt()));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedJdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        Number key = keyHolder.getKey();
        if (key != null) {
            task.setId(key.longValue());
        }
        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        String sql = "SELECT * FROM task WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        List<Task> tasks = namedJdbcTemplate.query(sql, params, taskRowMapper);
        return tasks.stream().findFirst();
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        String sql = "SELECT * FROM task WHERE created_by = :userId AND created_at BETWEEN :from AND :to";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("from", Timestamp.valueOf(from))
                .addValue("to", Timestamp.valueOf(to));
        return namedJdbcTemplate.query(sql, params, taskRowMapper);
    }

    @Override
    public void update(Task task) throws TaskNotFoundException {
        String sql = "UPDATE task SET title = :title, status = :status WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", task.getTitle())
                .addValue("status", task.getStatus().name())
                .addValue("id", task.getId());
        int rows = namedJdbcTemplate.update(sql, params);
        if (rows == 0) {
            throw new TaskNotFoundException(task.getId());
        }
    }

    @Override
    public void deleteById(long id) throws TaskNotFoundException {
        String sql = "DELETE FROM task WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        int rows = namedJdbcTemplate.update(sql, params);
        if (rows == 0) {
            throw new TaskNotFoundException(id);
        }
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        Set<String> activeStatuses = TaskStatus.getActiveStatusNames();
        String sql = "SELECT COUNT(*) FROM task WHERE created_by = :userId AND status IN (:statuses)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("statuses", activeStatuses);
        Long count = namedJdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0;
    }
}
