package com.lawpavilion.backend.service;

import com.lawpavilion.backend.dto.request.BookRequest;
import com.lawpavilion.backend.dto.request.UpdateBookRequest;
import com.lawpavilion.backend.dto.response.BookResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    BookResponse addBook(BookRequest request);
    List<BookResponse> getAllBooks(Pageable pageable);
    BookResponse updateBook(Long id, UpdateBookRequest request);
    String deleteBook(Long id);
}
