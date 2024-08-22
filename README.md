### README.md

# 📚 Student Payment Application

Welcome to the Student Payment Application! This application enables efficient management of students and their payments.

## 🌟 Features Overview
- View list of students and their payments
- Import student and payment lists
- Retrieve student info by code
- Filter students by study program
- Filter payments by status or type
- Retrieve payment by ID
- Update payment status
- Add new student payments

## 🛠️ Backend Documentation

### Endpoints
Accessible via Swagger-UI:
- **Students**
  - `GET /students/all`: Retrieve all students
  - `GET /students/{programId}`: Retrieve students by program
  - `GET /students/{code}`: Retrieve student by code
- **Payments**
  - `GET /payments/all`: Retrieve all payments
  - `GET /payments/status/{status}`: Retrieve payments by status
  - `GET /payments/type/{type}`: Retrieve payments by type
  - `GET /payment/{id}`: Retrieve payment by ID
  - `GET /payments/student/{code}`: Retrieve payments by student code
  - `PUT /payments/{id}`: Update payment status
  - `POST /payments/new`: Add new payment

### Entities
- **Student**: Attributes include ID, first name, last name, email, code, program ID
- **Payment**: Attributes include ID, date, amount, type, status, receipt

### Repositories
- **StudentRepository**: Access student data
- **PaymentRepository**: Access payment data

### Services
- **StudentService**: Business logic for students
- **PaymentService**: Business logic for payments

### Web Controllers
- **StudentRestController**: Handles student-related requests
- **PaymentRestController**: Handles payment-related requests

## 🤝 Contribution
Contributions are welcome! Please submit a pull request or open an issue to discuss your changes.

## 📄 License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## ℹ️ About
Developed by [Dev7HD](https://github.com/Dev7HD).
