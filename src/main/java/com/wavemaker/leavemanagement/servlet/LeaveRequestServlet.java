package com.wavemaker.leavemanagement.servlet;

import com.wavemaker.leavemanagement.model.Employee;
import com.wavemaker.leavemanagement.model.LeaveRequest;
import com.wavemaker.leavemanagement.repositories.EmployeeRepository;
import com.wavemaker.leavemanagement.repositories.LeaveRequestRepository;
import com.wavemaker.leavemanagement.repositories.impl.EmployeeRepositoryImpl;
import com.wavemaker.leavemanagement.repositories.impl.LeaveRequestRepositoryImpl;
import com.wavemaker.leavemanagement.service.EmployeeService;
import com.wavemaker.leavemanagement.service.LeaveRequestService;
import com.wavemaker.leavemanagement.service.impl.EmployeeServiceImpl;
import com.wavemaker.leavemanagement.service.impl.LeaveRequestServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/leaveRequest")
public class LeaveRequestServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LeaveRequestServlet.class.getName());
    private LeaveRequestRepository leaveRequestRepository;
    private LeaveRequestService leaveRequestService;
    private EmployeeService employeeService;

    @Override
    public void init() {
        leaveRequestRepository = new LeaveRequestRepositoryImpl();
        leaveRequestService = new LeaveRequestServiceImpl(leaveRequestRepository);
        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
        employeeService = new EmployeeServiceImpl(employeeRepository);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in");
            return;
        }

        String email = (String) session.getAttribute("email");
        if (email == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No email found in session");
            return;
        }

        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
        EmployeeService employeeService = new EmployeeServiceImpl(employeeRepository);
        JSONArray jsonArray = new JSONArray();

        try {
            Employee employee = employeeService.getEmployeeByEmail(email);
            if (employee == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }

            int employeeId = employee.getEmployeeId();
            List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByEmployeeId(employeeId);
            for (LeaveRequest leaveRequest : leaveRequests) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("leaveRequestId", leaveRequest.getLeaveRequestId());
                jsonObject.put("leaveType", leaveRequest.getLeaveType());
                jsonObject.put("fromDate", leaveRequest.getFromDate().toString());
                jsonObject.put("toDate", leaveRequest.getToDate().toString());
                jsonObject.put("reason", leaveRequest.getReason());
                jsonObject.put("status", leaveRequest.getStatus());
                jsonArray.put(jsonObject);
            }

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(jsonArray.toString());
            out.flush();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching leave requests", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to fetch leave requests: " + e.getMessage());
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in");
            return;
        }

        String email = (String) session.getAttribute("email");
        if (email == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No email found in session");
            return;
        }

        try {
            // Read JSON body from request
            ObjectMapper objectMapper = new ObjectMapper();
            LeaveRequest leaveRequest = objectMapper.readValue(request.getInputStream(), LeaveRequest.class);

            Employee employee = employeeService.getEmployeeByEmail(email);
            if (employee == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }

            int employeeId = employee.getEmployeeId();
            // Set additional fields
            leaveRequest.setEmployeeId(employeeId);
            leaveRequest.setStatus("Pending");

            // Save to database
            LeaveRequest leaveRequestAdded = leaveRequestService.addLeaveRequestToDb(leaveRequest);
            if (leaveRequestAdded != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Leave request submitted successfully.");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error adding leave request.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error submitting leave request", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to submit leave request: " + e.getMessage());
        }
    }
}

//    private void updateLeaveBalance(int employeeId, LeaveRequest leaveRequest) throws ServletException, SQLException {
//        String dbUrl = "jdbc:mysql://localhost:3306/LEAVE_MANAGEMENT";
//        String dbUser = "root";
//        String dbPassword = "87707@";
//
//        // Check current leave balance
//        String selectQuery = "SELECT sick_leave_available, pto_available, vacation_available, personal_leave_available " +
//                "FROM leave_balance WHERE employee_id = ?";
//
//
//        // Update leave balance query
//        String updateQuery = "UPDATE leave_balance SET " +
//                "sick_leave_available = sick_leave_available - ?, " +
//                "pto_available = pto_available - ?, " +
//                "vacation_available = vacation_available - ?, " +
//                "personal_leave_available = personal_leave_available - ? " +
//                "WHERE employee_id = ?";
//
//        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
//            // Start a transaction
//            connection.setAutoCommit(false);
//
//            // Check current leave balance
//            int sickLeaveAvailable = 0, ptoAvailable = 0, vacationAvailable = 0, personalLeaveAvailable = 0;
//            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
//                selectStatement.setInt(1, employeeId);
//                try (ResultSet resultSet = selectStatement.executeQuery()) {
//                    if (resultSet.next()) {
//                        sickLeaveAvailable = resultSet.getInt("sick_leave_available");
//                        ptoAvailable = resultSet.getInt("pto_available");
//                        vacationAvailable = resultSet.getInt("vacation_available");
//                        personalLeaveAvailable = resultSet.getInt("personal_leave_available");
//                    } else {
//                        LOGGER.log(Level.WARNING, "No leave balance record found for employee ID: {0}", employeeId);
//                        connection.rollback();
//                        return;
//                    }
//                }
//            }

//            int days = leaveRequest.getNumberOfDays();
//            boolean canProcess = false;
//
//            switch (leaveRequest.getLeaveType()) {
//                case "Sick Leave":
//                    if (sickLeaveAvailable >= days) {
//                        sickLeaveAvailable -= days;
//                        canProcess = true;
//                    }
//                    break;
//                case "PTO":
//                    if (ptoAvailable >= days) {
//                        ptoAvailable -= days;
//                        canProcess = true;
//                    }
//                    break;
//                case "Vacation":
//                    if (vacationAvailable >= days) {
//                        vacationAvailable -= days;
//                        canProcess = true;
//                    }
//                    break;
//                case "Personal Leave":
//                    if (personalLeaveAvailable >= days) {
//                        personalLeaveAvailable -= days;
//                        canProcess = true;
//                    }
//                    break;
//                default:
//                    throw new IllegalArgumentException("Unknown leave type: " + leaveRequest.getLeaveType());
//            }
//
//            if (!canProcess) {
//                LOGGER.log(Level.WARNING, "Insufficient leave balance for employee ID: {0} for leave type: {1}", new Object[]{employeeId, leaveRequest.getLeaveType()});
//                connection.rollback();
//                throw new SQLException("Insufficient leave balance");
//            }
//
//            // Update leave balance
//            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
//                updateStatement.setInt(1, leaveRequest.getLeaveType().equals("Sick Leave") ? days : 0);
//                updateStatement.setInt(2, leaveRequest.getLeaveType().equals("PTO") ? days : 0);
//                updateStatement.setInt(3, leaveRequest.getLeaveType().equals("Vacation") ? days : 0);
//                updateStatement.setInt(4, leaveRequest.getLeaveType().equals("Personal Leave") ? days : 0);
//                updateStatement.setInt(5, employeeId);
//                int rowsUpdated = updateStatement.executeUpdate();
//                if (rowsUpdated == 0) {
//                    LOGGER.log(Level.WARNING, "No leave balance record updated for employee ID: {0}", employeeId);
//                    connection.rollback();
//                    return;
//                }
//            }

            // Commit transaction

//            connection.commit();
//            LOGGER.log(Level.INFO, "Updated leave balance for employee ID: {0}", employeeId);
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error updating leave balance for employee ID: {0}", employeeId);
//            throw e;
//        }
//    }
//}
