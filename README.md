# 📚 Payments Management Application

Welcome to the Student Payment Application! This application enables efficient management of students and their payments.

# Student and Payment Management System

This project is a Spring Boot application that provides APIs for managing students and payments within an educational institution. It supports operations such as retrieving, adding, updating, and deleting students and payments.

## Features

- **Student Management:**
  - Get all students
  - Get all admins
  - Find students by program
  - Find a student by their code or email
  - Delete a user by email
  - Add new students and admins
  - Change and reset user passwords

- **Payment Management:**
  - Get all payments
  - Get payments by student code
  - Get payment by ID
  - Get payments by status or type
  - Update payment status
  - Add new payments with file upload
  - Retrieve payment receipts
  - Get payment status changes

## API Endpoints

### Student Management

- **Get all students**
  ```http
  GET /student/all
  ```

- **Get all admins**
  ```http
  GET /admin/all
  ```

- **Find students by program**
  ```http
  GET /student/program/{programId}
  ```

- **Find student by code**
  ```http
  GET /student/code/{code}
  ```

- **Get student by email**
  ```http
  GET /student/email/{email}
  ```

- **Delete user by email**
  ```http
  DELETE /delete
  ```

- **Add new student**
  ```http
  POST /student/new
  ```

- **Add new admin**
  ```http
  POST /admin/new
  ```

- **Change user password**
  ```http
  PUT /change-pw
  ```

- **Reset user password**
  ```http
  PUT /{email}/reset-pw
  ```

### Payment Management

- **Get all payments**
  ```http
  GET /all
  ```

- **Get all payments by student code**
  ```http
  GET /student/{code}
  ```

- **Get payment by ID**
  ```http
  GET /{id}
  ```

- **Get payments by status**
  ```http
  GET /status/{status}
  ```

- **Get payments by type**
  ```http
  GET /type/{type}
  ```

- **Update payment status**
  ```http
  PUT /{id}
  ```

- **Add new payment**
  ```http
  POST /new
  ```

- **Get payment receipt**
  ```http
  GET /receipt/{paymentId}
  ```

- **Get payment status changes**
  ```http
  GET /changes
  ```

## Dependencies

- Spring Boot
- Spring Security
- JWT for authentication
- ModelMapper
- Multipart file handling

## Web Controllers
- **StudentRestController**: Handles student-related requests
- **PaymentRestController**: Handles payment-related requests

## Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Dev7HD/Back-End-Student-Payment-App
   ```

2. **Navigate to the project directory:**
   ```bash
   cd Back-End-Student-Payment-App
   ```

3. **Install dependencies:**
   ```bash
   ./mvnw install
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## Contributing

Feel free to submit pull requests and report issues. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contribution
Contributions are welcome! Please submit a pull request or open an issue to discuss your changes.

## 📄 License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## ℹ️ About
Developed by [Dev7HD](https://github.com/Dev7HD).
