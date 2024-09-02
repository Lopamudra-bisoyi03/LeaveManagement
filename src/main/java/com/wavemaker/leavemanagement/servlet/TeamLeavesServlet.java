package com.wavemaker.leavemanagement.servlet;

import com.wavemaker.leavemanagement.model.LeaveRequest;
import com.wavemaker.leavemanagement.service.LeaveRequestService;
import com.wavemaker.leavemanagement.service.impl.LeaveRequestServiceImpl;
import com.wavemaker.leavemanagement.repositories.LeaveRequestRepository;
import com.wavemaker.leavemanagement.repositories.impl.LeaveRequestRepositoryImpl;

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

@WebServlet("/fetchTeamLeaves")
public class TeamLeavesServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(TeamLeavesServlet.class.getName());
    private final LeaveRequestService leaveRequestService;

    public TeamLeavesServlet() {
        LeaveRequestRepository leaveRequestRepository = new LeaveRequestRepositoryImpl();
        this.leaveRequestService = new LeaveRequestServiceImpl(leaveRequestRepository);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userRole") == null || !"manager".equals(session.getAttribute("userRole"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to access this page.");
            return;
        }

        String managerIdParam = request.getParameter("managerId");
        if (managerIdParam == null || managerIdParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or empty managerId parameter");
            return;
        }

        int managerId;
        try {
            managerId = Integer.parseInt(managerIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid managerId format");
            return;
        }

        JSONArray jsonArray = new JSONArray();
        try {
            List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsForManager(managerId);
            for (LeaveRequest leaveRequest : leaveRequests) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("leaveRequestId", leaveRequest.getLeaveRequestId());
                jsonObject.put("employeeId", leaveRequest.getEmployeeId());
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
            LOGGER.log(Level.SEVERE, "Error fetching team leaves", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to fetch team leaves: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userRole") == null || !"manager".equals(session.getAttribute("userRole"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to perform this action.");
            return;
        }

        String leaveRequestIdParam = request.getParameter("leaveRequestId");
        String action = request.getParameter("action");

        if (leaveRequestIdParam == null || leaveRequestIdParam.trim().isEmpty() || action == null || action.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        int leaveRequestId;
        try {
            leaveRequestId = Integer.parseInt(leaveRequestIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid leaveRequestId format");
            return;
        }

        if (!"approve".equalsIgnoreCase(action) && !"reject".equalsIgnoreCase(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action parameter");
            return;
        }

        try {
            boolean success = leaveRequestService.updateLeaveRequestStatus(leaveRequestId, action);
            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Leave request " + action + "d successfully.");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error performing action on leave request.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating leave request status", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update leave request status: " + e.getMessage());
        }
    }
}

