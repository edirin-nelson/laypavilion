package com.lawpavilion.backend.dto.response;

import com.lawpavilion.backend.entity.Book;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private LocalDate publishedDate;

    public BookResponse (Book book){
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.isbn = book.getIsbn();
        this.publishedDate = book.getPublishedDate();
    }
}
