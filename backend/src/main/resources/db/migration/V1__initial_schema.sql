CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(20) NOT NULL,
    verified BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    organization_name VARCHAR(180),
    address VARCHAR(500),
    city VARCHAR(80),
    state VARCHAR(80),
    pincode VARCHAR(12),
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE donations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    donor_id BIGINT NOT NULL,
    food_name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(80),
    quantity DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    food_type VARCHAR(24) NOT NULL,
    preparation_time DATETIME(6),
    use_by_time DATETIME(6),
    pickup_deadline DATETIME(6) NOT NULL,
    storage_conditions VARCHAR(250),
    allergen_information VARCHAR(250),
    packaging_information VARCHAR(250),
    image_url VARCHAR(700),
    address VARCHAR(400) NOT NULL,
    city VARCHAR(80) NOT NULL,
    state VARCHAR(80),
    pincode VARCHAR(12),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL,
    safety_confirmed BOOLEAN NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_donations_donor FOREIGN KEY (donor_id) REFERENCES users (id)
);

CREATE INDEX idx_donation_status_deadline ON donations (status, pickup_deadline);
CREATE INDEX idx_donation_city ON donations (city);

CREATE TABLE donation_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    donation_id BIGINT NOT NULL,
    ngo_id BIGINT NOT NULL,
    message VARCHAR(500),
    requested_quantity DECIMAL(10, 2) NOT NULL,
    request_status VARCHAR(20) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_donation_ngo UNIQUE (donation_id, ngo_id),
    CONSTRAINT fk_donation_requests_donation FOREIGN KEY (donation_id) REFERENCES donations (id),
    CONSTRAINT fk_donation_requests_ngo FOREIGN KEY (ngo_id) REFERENCES users (id)
);

CREATE TABLE deliveries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    volunteer_id BIGINT,
    status VARCHAR(24) NOT NULL,
    verification_code VARCHAR(8) NOT NULL,
    pickup_time DATETIME(6),
    delivered_time DATETIME(6),
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_deliveries_request UNIQUE (request_id),
    CONSTRAINT fk_deliveries_request FOREIGN KEY (request_id) REFERENCES donation_requests (id),
    CONSTRAINT fk_deliveries_volunteer FOREIGN KEY (volunteer_id) REFERENCES users (id)
);

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(140) NOT NULL,
    message VARCHAR(700) NOT NULL,
    type VARCHAR(40),
    is_read BOOLEAN NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_notification_user_read ON notifications (user_id, is_read);
