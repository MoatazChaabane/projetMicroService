-- Script SQL pour créer la base de données MySQL
-- Exécutez ce script dans MySQL pour créer la base de données
-- Accès phpMyAdmin: http://localhost:9090/phpmyadmin/index.php?route=/database/structure&db=medicalapp

CREATE DATABASE IF NOT EXISTS medicalapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medicalapp;

-- La table users sera créée automatiquement par Hibernate avec la configuration
-- spring.jpa.hibernate.ddl-auto=update
-- Mais voici le script SQL si vous voulez le créer manuellement :

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    photo_url VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

