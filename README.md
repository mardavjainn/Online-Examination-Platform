# Online Examination Platform

A microservices-based online examination platform built using Spring Boot, Spring Security, JWT authentication, and a React frontend.

## System Overview

The platform consists of four independent microservices and a single-page React frontend:

- **Auth Service (Port 8080)**: Manages user registration, authentication, and JWT token issuance with role-based permissions (Student, Teacher, Admin).
- **Exam Service (Port 8082)**: Handles exam creation, question management, option configurations, and exam publishing. Creating or editing exams requires Teacher or Admin privileges.
- **Submission Service (Port 8083)**: Controls student exam attempts, question fetching, answer saving, and final exam submissions.
- **Result Service (Port 8084)**: Evaluates submitted exams, calculates percentages and scores, and provides result lookup APIs.
- **Frontend (Port 5173)**: React and Vite single-page dashboard for user login, exam administration, test-taking, and viewing results.

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.3.4, Spring Security, Spring Data JPA
- **Security**: JWT (io.jsonwebtoken)
- **Frontend**: React 18, Vite
- **Database**: MySQL (auth_db, exam_db, submission_db, result_db)
- **Build Tool**: Maven

## API Endpoints

### Auth Service (Port 8080)
- `POST /auth/register` - Register a new user
- `POST /auth/login` - Authenticate user and receive JWT

### Exam Service (Port 8082)
- `GET /api/exams/published` - Get all published exams
- `POST /api/exams` - Create an exam (Teacher/Admin)
- `POST /api/exams/{id}/questions` - Add question (Teacher/Admin)
- `POST /api/questions/{id}/options` - Add option (Teacher/Admin)

### Submission Service (Port 8083)
- `POST /api/submissions/start` - Start an exam attempt
- `GET /api/submissions/{id}/questions` - Fetch questions for an attempt
- `POST /api/submissions/answer` - Save student answer choice
- `POST /api/submissions/submit` - Finalize attempt and trigger grading

### Result Service (Port 8084)
- `POST /results/grade` - Evaluate attempt payload
- `GET /results/attempt/{attemptId}` - Get result by attempt ID
- `GET /results/student/{studentId}` - Get results for a student

## Getting Started

### Prerequisites
- Java 17 or higher
- Node.js and npm
- MySQL Server

### Database Configuration
Create the required databases in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS exam_db;
CREATE DATABASE IF NOT EXISTS submission_db;
CREATE DATABASE IF NOT EXISTS result_db;
```

### Running the Platform

1. **Start the Microservices**:
   Run each service in a separate terminal:
   ```bash
   cd auth-service && mvn spring-boot:run
   cd exam-service && mvn spring-boot:run
   cd submission-service && mvn spring-boot:run
   cd result-service && mvn spring-boot:run
   ```

2. **Start the Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Access the dashboard at `http://localhost:5173`.

## Future Improvements

- Add Eureka Service Discovery and API Gateway
- Containerize services using Docker