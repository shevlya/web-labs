package ru.ssau.todo.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("jdbc")
public class TaskJdbcRepository implements TaskRepository {
    private final JdbcTemplate jdbcTemplate;

    public TaskJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Task> taskRowMapper = (rs, rowNum) -> {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setCreatedBy(rs.getLong("created_by"));

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            task.setCreatedAt(timestamp.toLocalDateTime());
        }
        return task;
    };

    @Override
    public Task create(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().isBlank() || task.getStatus() == null) {
            throw new IllegalArgumentException("Task data is invalid");
        }

        String sql = "INSERT INTO task (title, status, created_by, created_at) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Устанавливаем время создания, если его нет
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getStatus().name());
            ps.setLong(3, task.getCreatedBy());
            ps.setTimestamp(4, Timestamp.valueOf(task.getCreatedAt()));
            return ps;
        }, keyHolder);

        // Получаем сгенерированный ID
        if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
            task.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        }
        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        String sql = "SELECT * FROM task WHERE id = ?";
        List<Task> tasks = jdbcTemplate.query(sql, taskRowMapper, id);
        return tasks.stream().findFirst();
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        LocalDateTime start = (from != null) ? from : LocalDateTime.of(2011, 9, 1, 8, 30);
        LocalDateTime end = (to != null) ? to : LocalDateTime.of(2026, 2, 13, 0, 0);

        String sql = "SELECT * FROM task WHERE created_by = ? AND created_at BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, taskRowMapper, userId, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }

    @Override
    public void update(Task task) {
        if (findById(task.getId()).isEmpty()) {
            throw new TaskNotFoundException(task.getId());
        }

        String sql = "UPDATE task SET title = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(sql, task.getTitle(), task.getStatus().name(), task.getId());
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM task WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        String sql = "SELECT COUNT(*) FROM task WHERE created_by = ? AND (status = 'OPEN' OR status = 'IN_PROGRESS')";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
        return count != null ? count : 0;
    }
}
