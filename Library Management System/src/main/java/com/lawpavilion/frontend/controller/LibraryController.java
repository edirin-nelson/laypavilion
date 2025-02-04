package com.lawpavilion.frontend.controller;

import com.lawpavilion.frontend.pojo.Book;
import com.lawpavilion.frontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LibraryController {
    private static final Logger logger = Logger.getLogger(LibraryController.class.getName());
    private static final String BASE_URL = "http://localhost:8090/api/books";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, Long> idColumn;
    @FXML private TableColumn<Book, String> titleColumn, authorColumn, isbnColumn, publishedDateColumn;
    @FXML private TextField titleField, authorField, isbnField;
    @FXML private Button addButton, updateButton, deleteButton, refreshButton;

    private final ObservableList<Book> bookList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        logger.info("Initializing LibraryController");
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        previousButton.setDisable(true);
        pageLabel.setText("Page 1");

        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean bookSelected = newSelection != null;
            updateButton.setDisable(!bookSelected);
            deleteButton.setDisable(!bookSelected);

            if (bookSelected) {
                titleField.setText(newSelection.getTitle());
                authorField.setText(newSelection.getAuthor());
                isbnField.setText(newSelection.getIsbn());
            }
        });

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publishedDateColumn.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));

        bookTable.setItems(bookList);
        refreshBooks();
    }

    @FXML
    private void addBook() {
        logger.info("Attempting to add a new book");
        if (titleField.getText().isEmpty() || authorField.getText().isEmpty() || isbnField.getText().isEmpty()) {
            showAlert("Validation Error", "All fields must be filled out");
            return;
        }

        String json = String.format(
                "{\"title\":\"%s\",\"author\":\"%s\",\"isbn\":\"%s\"}",
                titleField.getText(), authorField.getText(), isbnField.getText()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 201) {
                            logger.info("Book added successfully");
                            clearFields();
                            refreshBooks();
                        } else {
                            logger.warning("Failed to add book. Status code: " + response.statusCode());
                            showAlert("Error", "Failed to add book. Status code: " + response.statusCode());
                        }
                    });
                })
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to send request: " + e.getMessage(), e);
                    Platform.runLater(() -> showAlert("Error", "Failed to send request: " + e.getMessage()));
                    return null;
                });
    }

    private void clearFields() {
        logger.info("Clearing input fields");
        titleField.clear();
        authorField.clear();
        isbnField.clear();
    }

    @FXML
    private void updateBook() {
        logger.info("Attempting to update a book");
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("Error", "No book selected!");
            return;
        }

        String json = String.format(
                "{\"id\":%d,\"title\":\"%s\",\"author\":\"%s\",\"isbn\":\"%s\"}",
                selectedBook.getId(), titleField.getText(), authorField.getText(), isbnField.getText()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + selectedBook.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenRun(() -> {
                    Platform.runLater(this::clearFields);
                    refreshBooks();
                })
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to send update request: " + e.getMessage(), e);
                    Platform.runLater(() -> showAlert("Error", "Failed to update book: " + e.getMessage()));
                    return null;
                });
    }

    @FXML
    private void deleteBook() {
        logger.info("Attempting to delete a book");
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("Error", "No book selected!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this book?");
        alert.setContentText("Book: " + selectedBook.getTitle());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/" + selectedBook.getId()))
                        .DELETE()
                        .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenRun(this::refreshBooks)
                        .exceptionally(e -> {
                            logger.log(Level.SEVERE, "Failed to send delete request: " + e.getMessage(), e);
                            Platform.runLater(() -> showAlert("Error", "Failed to delete book: " + e.getMessage()));
                            return null;
                        });
            }
        });
    }

    @FXML
    private void refreshBooks() {
        logger.info("Refreshing book list");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?page=" + currentPage + "&size=" + PAGE_SIZE))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("HTTP Error: " + response.statusCode());
                    }
                    return response.body();
                })
                .thenAccept(responseBody -> {
                    try {
                        List<Book> books = Arrays.asList(JsonUtil.fromJson(responseBody, Book[].class));
                        Platform.runLater(() -> {
                            bookList.clear();
                            bookList.addAll(books);
                            updatePaginationControls(books.size());
                        });
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to parse book data", e);
                        Platform.runLater(() -> showAlert("Error", "Failed to parse book data."));
                    }
                })
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to refresh books: " + e.getMessage(), e);
                    Platform.runLater(() -> showAlert("Error", "Failed to refresh books: " + e.getMessage()));
                    return null;
                });
    }

    @FXML
    private void previousPage() {
        if (currentPage > 0) {
            logger.info("Navigating to previous page");
            currentPage--;
            refreshBooks();
        }
    }

    @FXML
    private void nextPage() {
        logger.info("Navigating to next page");
        currentPage++;
        refreshBooks();
    }

    private void updatePaginationControls(int currentPageSize) {
        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPageSize < PAGE_SIZE);
        pageLabel.setText("Page " + (currentPage + 1));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}