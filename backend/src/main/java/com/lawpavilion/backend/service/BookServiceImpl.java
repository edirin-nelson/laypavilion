package com.lawpavilion.backend.service;

import com.lawpavilion.backend.dto.request.BookRequest;
import com.lawpavilion.backend.dto.request.UpdateBookRequest;
import com.lawpavilion.backend.dto.response.BookResponse;
import com.lawpavilion.backend.entity.Book;
import com.lawpavilion.backend.exception.CustomException;
import com.lawpavilion.backend.exception.ResourceNotFoundException;
import com.lawpavilion.backend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public BookResponse addBook(BookRequest request) {
        logger.info("Attempting to add a new book with title: {}", request.getTitle());
        try {
            Book book = new Book(request);
            book.setPublishedDate(LocalDate.now());
            Book savedBook = bookRepository.save(book);
            logger.info("Book added successfully with ID: {}", savedBook.getId());
            return new BookResponse(savedBook);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments provided for adding a book", e);
            throw new CustomException("Invalid request data for adding book", e);
        } catch (Exception e) {
            logger.error("Unexpected error occurred while adding a book", e);
            throw new CustomException("Failed to add book due to an unexpected error", e);
        }
    }

    @Override
    public List<BookResponse> getAllBooks(Pageable pageable) {
        logger.info("Fetching all books with pagination");
        Page<Book> bookPage = bookRepository.findAll(pageable);
        logger.info("Fetched {} books", bookPage.getTotalElements());
        return bookPage.getContent().stream()
                .map(BookResponse::new)
                .toList();
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, UpdateBookRequest request) {
        logger.info("Attempting to update book with ID: {}", id);
        try {
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Book not found with id: %d", id)
                    ));
            Optional.ofNullable(request.getTitle()).ifPresent(book::setTitle);
            Optional.ofNullable(request.getAuthor()).ifPresent(book::setAuthor);
            Optional.ofNullable(request.getIsbn()).ifPresent(book::setIsbn);
            Book updatedBook = bookRepository.save(book);
            logger.info("Book updated successfully with ID: {}", updatedBook.getId());
            return new BookResponse(updatedBook);
        } catch (ResourceNotFoundException e) {
            logger.error("Book not found with ID: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while updating book with ID: {}", id, e);
            throw new CustomException("Failed to update book due to an unexpected error", e);
        }
    }

    @Override
    public String deleteBook(Long id) {
        logger.info("Attempting to delete book with ID: {}", id);
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            logger.info("Book deleted successfully with ID: {}", id);
            return "Book deleted successfully";
        }
        logger.error("Book not found with ID: {}", id);
        throw new ResourceNotFoundException("Book not found with id " + id);
    }
}