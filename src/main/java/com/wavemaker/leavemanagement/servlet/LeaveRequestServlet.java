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

//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        HttpSession session = request.getSession(false);
//        if (session == null) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in");
//            return;
//        }
//        String email = (String) session.getAttribute("email");
//        if (email == null) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No email found in session");
//            return;
//        }
//        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
//        EmployeeService employeeService = new EmployeeServiceImpl(employeeRepository);
//        JSONArray jsonArray = new JSONArray();
//        try {
//            Employee employee = employeeService.getEmployeeByEmail(email);
//            if (employee == null) {
//                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
//                return;
//            }
//            int employeeId = employee.getEmployeeId();
//            List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByEmployeeId(employeeId);
//            for (LeaveRequest leaveRequest : leaveRequests) {
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("leaveRequestId", leaveRequest.getLeaveRequestId());
//                jsonObject.put("leaveType", leaveRequest.getLeaveType());
//                jsonObject.put("fromDate", leaveRequest.getFromDate().toString());
//                jsonObject.put("toDate", leaveRequest.getToDate().toString());
//                jsonObject.put("reason", leaveRequest.getReason());
//                jsonObject.put("status", leaveRequest.getStatus());
//                jsonObject.put("createdAt", leaveRequest.getCreatedAt().toString());
//                jsonArray.put(jsonObject);
//            }
//
//            response.setContentType("application/json");
//            PrintWriter out = response.getWriter();
//            out.print(jsonArray.toString());
//            out.flush();
//
//        } catch (Exception e) {
//            LOGGER.log(Level.SEVERE, "Error fetching leave requests", e);
//            response.sendRedirect(request.getContextPath() + "/leaveRequest");
//        }
//    }
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
            ObjectMapper objectMapper = new  ObjectMapper();
            LeaveRequest leaveRequest = objectMapper.readValue(request.getInputStream(), LeaveRequest.class);

            Employee employee = employeeService.getEmployeeByEmail(email);
            if (employee == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }

            int employeeId = employee.getEmployeeId();

            // Set additional fields
            leaveRequest.setEmployeeId(employeeId);
            leaveRequest.setStatus("Pending"); // Assuming default status is "Pending"

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
