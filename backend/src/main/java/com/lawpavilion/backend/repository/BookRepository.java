package com.lawpavilion.backend.repository;

import com.lawpavilion.backend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
