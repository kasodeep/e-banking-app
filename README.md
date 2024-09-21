# Banking-App

**Complete Backend for Banking Application**

## Table of Contents
- [Description](#description)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Build and Run](#build-and-run)
- [API Documentation](#api-documentation)

## Description
- This Spring Boot application serves as the backend for a comprehensive e-banking system. 
- It handles user authentication using JWT, integrates with Twilio for notifications, and includes functionality for sending emails. 
- The application stores user information, account details, and generates transaction PINs. 
- It also maintains transaction records and can generate PDF reports when provided with start and end dates.

## Prerequisites
- Java Development Kit (JDK) version 21 or later
- MySQL database
- Twilio account for SMS notifications

## Getting Started
1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/Banking-App.git

## Build and Run
1. **To build the application and run it locally, use the following Maven command:**
   ```bash
   mvn spring-boot:run

## API Documentation
API documentation is generated using Spring-doc OpenAPI and is available at http://localhost:8080/swagger-ui.html.