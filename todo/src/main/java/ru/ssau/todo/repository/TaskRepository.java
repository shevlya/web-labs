package ru.ssau.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query(value = "SELECT * FROM task WHERE created_by = :userId AND created_at BETWEEN COALESCE(:from, '-infinity'::timestamp) AND COALESCE(:to, 'infinity'::timestamp)", nativeQuery = true)
    List<Task> findAll(@Param("userId") Long userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.createdBy.id = :userId AND t.status IN :statuses")
    long countActiveByUserId(@Param("userId") Long userId, @Param("statuses") Set<TaskStatus> statuses);
}