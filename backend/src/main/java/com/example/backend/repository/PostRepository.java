package com.example.backend.repository;

import com.example.backend.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"author"})
    Page<Post> findAllByOrderByTimestampDesc(Pageable pageable);
    
    List<Post> findByAuthorIdOrderByTimestampDesc(Long authorId);
    
    List<Post> findByAuthorId(Long authorId);
}
