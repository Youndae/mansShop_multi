CREATE TABLE IF NOT EXISTS member (
    userId VARCHAR(50) PRIMARY KEY,
    userPW VARCHAR(255),
    userName VARCHAR(100) NOT NULL,
    nickname VARCHAR(100),
    userEmail VARCHAR(100) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    memberPoint INT DEFAULT 0,
    phone VARCHAR(20),
    birth DATE,
    createdAt DATETIME(3) DEFAULT NOW(3),
    updatedAt DATETIME(3) DEFAULT NOW(3)
);

CREATE TABLE IF NOT EXISTS auth (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    auth VARCHAR(50) NOT NULL,
    CONSTRAINT member_auth_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS classification (
    id VARCHAR(100) PRIMARY KEY,
    classificationStep INT NOT NULL
);

CREATE TABLE IF NOT EXISTS product (
    id VARCHAR(200) PRIMARY KEY,
    classificationId VARCHAR(100),
    productName VARCHAR(200) NOT NULL,
    productPrice INT NOT NULL,
    thumbnail VARCHAR(255) NOT NULL,
    isOpen TINYINT(1) DEFAULT 1 NOT NULL,
    productSales BIGINT DEFAULT 0 NOT NULL,
    productDiscount INT DEFAULT 0 NOT NULL,
    createdAt DATETIME(3) DEFAULT NOW(3),
    updatedAt DATETIME(3) DEFAULT NOW(3),
    CONSTRAINT classification_product_FK FOREIGN KEY (classificationId)
    REFERENCES classification(id)
    ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS productOption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    productId VARCHAR(200) NOT NULL,
    size VARCHAR(20),
    color VARCHAR(50),
    stock INT DEFAULT 0 NOT NULL,
    isOpen TINYINT(1) DEFAULT 1 NOT NULL,
    CONSTRAINT product_option_FK FOREIGN KEY (productId)
    REFERENCES product (id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS productThumbnail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    productId VARCHAR(200) NOT NULL,
    imageName VARCHAR(255) NOT NULL,
    CONSTRAINT product_thumbnail_FK FOREIGN KEY (productId)
    REFERENCES product (id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS productInfoImage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    productId VARCHAR(200) NOT NULL,
    imageName VARCHAR(255) NOT NULL,
    CONSTRAINT product_infoImage_FK FOREIGN KEY (productId)
    REFERENCES product (id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    cookieId VARCHAR(255),
    createdAt DATE DEFAULT (CURDATE()) NOT NULL,
    updatedAt DATE DEFAULT (CURDATE()) NOT NULL,
    CONSTRAINT member_cart_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cartDetail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cartId BIGINT NOT NULL,
    productOptionId BIGINT NOT NULL,
    cartCount INT,
    CONSTRAINT cart_detail_FK FOREIGN KEY (cartId)
    REFERENCES cart (id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT option_cartDetail_FK FOREIGN KEY (productOptionId)
    REFERENCES productOption (id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS productLike (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    productId VARCHAR(200) NOT NULL,
    createdAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    CONSTRAINT member_productLike_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE,
    CONSTRAINT product_like_FK FOREIGN KEY (productId)
    REFERENCES product (id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS qnaClassification(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    qnaClassificationName VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS memberQnA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    qnaClassificationId BIGINT NOT NULL,
    memberQnATitle VARCHAR(200) NOT NULL,
    memberQnAContent TEXT,
    createdAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    updatedAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    memberQnAStat TINYINT(1) DEFAULT 0,
    CONSTRAINT member_qna_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE,
    CONSTRAINT classification_qna_FK FOREIGN KEY (qnaClassificationId)
    REFERENCES qnaClassification(id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS memberQnAReply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    qnaId BIGINT NOT NULL,
    replyContent TEXT,
    createdAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    updatedAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    CONSTRAINT member_qna_reply_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE,
    CONSTRAINT memberQnA_reply_FK FOREIGN KEY (qnaId)
    REFERENCES memberQnA (id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS productOrder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    recipient VARCHAR(50) NOT NULL,
    orderPhone VARCHAR(100) NOT NULL,
    orderAddress VARCHAR(200) NOT NULL,
    orderMemo VARCHAR(200),
    orderTotalPrice INT NOT NULL,
    deliveryFee INT,
    createdAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    paymentType VARCHAR(10) NOT NULL,
    orderStat VARCHAR(20) NOT NULL,
    productCount INT NOT NULL,
    CONSTRAINT member_order_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS productOrderDetail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    productOptionId BIGINT,
    productId VARCHAR(200),
    orderId BIGINT NOT NULL,
    orderDetailCount INT NOT NULL,
    orderDetailPrice INT NOT NULL,
    orderReviewStatus TINYINT(1) DEFAULT 0 NOT NULL,
    CONSTRAINT productOption_orderDetail_FK FOREIGN KEY (productOptionId)
    REFERENCES productOption (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT product_orderDetail_FK FOREIGN KEY (productId)
    REFERENCES product (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT order_detail_FK FOREIGN KEY (orderId)
    REFERENCES productOrder (id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS productQnA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    productId VARCHAR(200) NOT NULL,
    qnaContent TEXT,
    createdAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    updatedAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    productQnAStat TINYINT(1) DEFAULT 0 NOT NULL,
    CONSTRAINT member_productQnA_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE,
    CONSTRAINT product_qna_FK FOREIGN KEY (productId)
    REFERENCES product (id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS productQnAReply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    qnaId BIGINT NOT NULL,
    replyContent TEXT,
    createdAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    updatedAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    CONSTRAINT member_productQnAReply_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE,
    CONSTRAINT productQnA_reply_FK FOREIGN KEY (qnaId)
    REFERENCES productQnA (id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS productReview (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    productId VARCHAR(200) NOT NULL,
    reviewContent TEXT NOT NULL,
    productOptionId BIGINT NOT NULL,
    createdAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    updatedAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    status TINYINT(1) DEFAULT 0 NOT NULL,
    CONSTRAINT member_review_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE,
    CONSTRAINT product_review_FK FOREIGN KEY (productId)
    REFERENCES product (id)
    ON DELETE CASCADE,
    CONSTRAINT productOption_review_FK FOREIGN KEY (productOptionId)
    REFERENCES productOption (id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS productReviewReply(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    reviewId BIGINT NOT NULL,
    replyContent TEXT,
    createdAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    updatedAt DATETIME(3) DEFAULT NOW(3) NOT NULL,
    CONSTRAINT member_reviewReply_FK FOREIGN KEY (userId)
    REFERENCES member (userId)
    ON DELETE CASCADE,
    CONSTRAINT review_reply_FK FOREIGN KEY (reviewId)
    REFERENCES productReview (id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS periodSalesSummary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period DATE DEFAULT (CURDATE()) NOT NULL,
    sales BIGINT NOT NULL,
    salesQuantity BIGINT NOT NULL,
    orderQuantity BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS productSalesSummary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    periodMonth DATE DEFAULT (CURDATE()) NOT NULL,
    classificationId VARCHAR(100),
    productId VARCHAR(200),
    productOptionId BIGINT,
    sales BIGINT NOT NULL,
    salesQuantity BIGINT NOT NULL,
    orderQuantity BIGINT NOT NULL,
    CONSTRAINT classification_sales_FK FOREIGN KEY (classificationId)
    REFERENCES classification (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT product_sales_FK FOREIGN KEY (productId)
    REFERENCES product (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT productOption_sales_FK FOREIGN KEY (productOptionId)
    REFERENCES productOption (id)
    ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    relatedId BIGINT,
    isRead TINYINT(1) DEFAULT 0 NOT NULL,
    createdAt DATETIME(3) DEFAULT NOW(3),
    CONSTRAINT member_notification_FK FOREIGN KEY (userId)
    REFERENCES member(userId)
    ON UPDATE CASCADE ON DELETE CASCADE
);