package com.example.backend.repository;

import com.example.backend.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAuthorIdOrderByTimestampDesc(Long authorId);
}
