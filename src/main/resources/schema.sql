-- 图书表
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS books;

CREATE TABLE books (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn              VARCHAR(32)  NOT NULL,
    title             VARCHAR(200) NOT NULL,
    author            VARCHAR(100) NOT NULL,
    type              VARCHAR(32)  NOT NULL,
    description       VARCHAR(1000),
    total_copies      INT          NOT NULL,
    available_copies  INT          NOT NULL,
    CONSTRAINT uk_books_isbn UNIQUE (isbn),
    CONSTRAINT ck_books_copies CHECK (available_copies >= 0 AND available_copies <= total_copies)
);

-- 借阅单表
CREATE TABLE loans (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      VARCHAR(64)  NOT NULL,
    book_id      BIGINT       NOT NULL,
    status       VARCHAR(32)  NOT NULL,
    borrowed_at  TIMESTAMP    NOT NULL,
    due_at       TIMESTAMP    NOT NULL,
    returned_at  TIMESTAMP,
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books (id)
);

CREATE INDEX idx_loans_user_status ON loans (user_id, status);
CREATE INDEX idx_loans_user_book_status ON loans (user_id, book_id, status);
