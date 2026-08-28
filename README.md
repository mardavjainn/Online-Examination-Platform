# Online Examination Platform

A microservices-based online examination system built with Spring Boot and JWT authentication.

---

## About

This project provides a secure and scalable backend for conducting online exams. It currently includes two independent microservices:

- **Auth Service** – Handles user registration, login, and JWT-based authentication.
- **Exam Service** – Manages exams, questions, and options with role-based access.

Both services are designed to work independently and communicate through shared JWT tokens.

---

## Services Overview

### Auth Service
- User registration with BCrypt password encryption
- Login with JWT token generation
- Role-based access control (Student, Teacher, Admin)
- Stateless authentication using Spring Security

### Exam Service
- Create, view, update, and delete exams
- Add questions and options to exams
- Publish or unpublish exams
- Validation before publishing (minimum questions and options)
- Soft delete support
- Audit fields (created by, created date, updated date)

---

## Tech Stack

- Java 17
- Spring Boot 3.3.4
- Spring Security
- JWT (JJWT)
- Spring Data JPA
- MySQL
- Maven
- Lombok

---

## Getting Started

### Prerequisites

- Java 17
- MySQL
- Maven

### Database Setup

Create two databases:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE exam_db;