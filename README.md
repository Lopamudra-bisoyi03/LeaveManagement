# LeaveManagement

## Project Description
*LeaveManagement* is a web application designed to simplify the leave management process in organizations. 
Employees can easily apply for different types of leave, and managers can approve or reject these requests.

## Table of Contents
- [Project Description](#project-description)
- [Table of Contents](#table-of-contents)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Configuration](#configuration)

## Features
- *Employee Login*
  - Apply for Leave
  - View Applied Leaves and their Status
  - Leave validation during apply for leave(no need to apply for leave on weekends or bank holidays)
  - Holiday calendar that will showcase the holidays of the current financial year.

- *Manager Login*
  - Approve or Reject Leave Requests
  - View List of Employees and their Leave Summary
  
## Installation
### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- MySQL or any relational database
- Postman (for API testing)
- A web browser (for accessing the UI)

### Steps to Install
1. *Clone the Repository:*
   bash
   git clone(https://github.com/Lopamudra-bisoyi03/LeaveManagement.git)
   cd LeaveManagement
   

2. *Set Up the Database:*
   - Create a MySQL database named LEAVE_MANAGEMENT.
   - Run the SQL scripts in the db/schema.sql file to create the necessary tables.
   - Insert sample data using the db/data.sql file.

3. *Build the Project:*
   mvn clean install


## Usage
### Employee
1. *Login*: Enter your credentials on the login page.
2. *Apply for Leave*: Navigate to the "Apply Leave" section, select the leave type, and submit your request.

### Manager
1. *Login*: Enter your credentials on the login page.
2. *Approve/Reject Leaves*: Review pending leave requests and approve or reject them.

## API Endpoints

### Authentication
- *POST* /api/login - Authenticate user and start a session.
- *POST* /api/logout - Logout and end the session.

### Employee
- *GET* /api/employees/{id}/leaves - Retrieve leaves for a specific employee.
- *POST* /api/employees/{id}/leaves - Apply for leave.

### Manager
- *GET* /api/managers/{id}/leaverequests - Retrieve pending leave requests.
- *POST* /api/managers/{id}/leaverequests/{requestId}/approve - Approve a leave request.
- *POST* /api/managers/{id}/leaverequests/{requestId}/reject - Reject a leave request.
- *GET* /api/managers/{id}/team - Get a list of employees under the manager.

## Database Schema
### Tables
- *Employee*:
EMPLOYEE_ID	int	NO	PRI		auto_increment
NAME	varchar(200)	NO			
EMAIL	varchar(300)	NO	UNI		
PHONE	varchar(20)	YES			
DOB	date	YES			
MANAGER_ID	int	YES	MUL		

- *LeaveRequest*: 
LEAVE_REQUEST_ID	int	NO	PRI		auto_increment
EMPLOYEE_ID	int	NO	MUL		
LEAVE_TYPE	enum('SICK','PTO','VACATION','PERSONAL')	NO			
FROM_DATE	date	NO			
TO_DATE	date	NO			
REASON	text	YES			
STATUS	enum('PENDING','APPROVED','REJECTED')	YES		PENDING

- *LoginCredentials*: 
LOGIN_CREDENTIALS_ID	int	NO	PRI		auto_increment
EMAIL	varchar(200)	NO	UNI		
PASSWORD	varchar(200)	NO			

### Relationships
- An *Employee* has a *Manager*.
- A *Manager* is also an *Employee*.
- *LeaveRequest* is associated with both *Employee* and *Manager*.

## Configuration
- *application.properties*: Set up the database connection, server port, and other configurations.
- *Custom Validation Rules*: Implement validation rules in both the UI and backend to ensure:
 - Leave dates are not holidays or weekends.

## Testing
### Using Postman
1. *Import Postman Collection*: Use the provided Postman collection to test the API endpoints.
2. *Run Tests*: Verify the functionality by sending requests and inspecting responses.

### Backend Testing
- Use JUnit and Mockito for unit tests.
- Run tests using Maven:
  mvn test
  

## Contributing
1. *Fork the Repository*: Create a fork of the repository on GitHub.
2. *Create a New Branch*: Develop your feature or fix on a new branch.
3. *Commit Your Changes*: Write clear and concise commit messages.
4. *Submit a Pull Request*: Explain your changes in detail in the pull request description.

