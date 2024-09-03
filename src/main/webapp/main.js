document.addEventListener('DOMContentLoaded', () => {
    const profileIcon = document.getElementById('profile-icon');
    const leaveRequestsContainer = document.getElementById('leave-requests');
    const teamLeavesSection = document.getElementById('team-leaves-section');
    const teamLeavesMessage = document.getElementById('team-leaves-message');
    const fromDateInput = document.getElementById('fromDate');
    const toDateInput = document.getElementById('toDate');
    const leaveRequestForm = document.getElementById('leaveRequestForm');
    const loginForm = document.querySelector('form[action="http://localhost:8088/loginCredentials"]');
    const fetchLeaveRequestsBtn = document.getElementById('fetchLeaveRequestsBtn');
    const fetchTeamLeavesBtn = document.getElementById('fetchTeamLeavesBtn');

    // Define bank holidays (in YYYY-MM-DD format)
    const bankHolidays = [
        '2025-01-01', '2025-01-26', '2025-03-14', '2024-04-11',
        '2024-04-14', '2024-04-27', '2024-09-07', '2024-09-16',
        '2024-10-02', '2024-10-13', '2024-10-31', '2024-12-25'
    ];

    const today = new Date().toISOString().split('T')[0];
    const weekends = [0, 6]; // Sunday and Saturday

    function isWeekend(date) {
        const day = new Date(date).getDay();
        return weekends.includes(day);
    }

    function isBankHoliday(date) {
        return bankHolidays.includes(date);
    }

    function updateDateValidationMessages() {
        const fromDate = fromDateInput.value;
        const toDate = toDateInput.value;

        document.getElementById('fromDateMessage').textContent = '';
        document.getElementById('toDateMessage').textContent = '';

        if (fromDate && (isWeekend(fromDate) || isBankHoliday(fromDate))) {
            document.getElementById('fromDateMessage').textContent = 'Selected date is a weekend or a bank holiday. Please choose another date.';
        }

        if (toDate && (isWeekend(toDate) || isBankHoliday(toDate))) {
            document.getElementById('toDateMessage').textContent = 'Selected date is a weekend or bank holiday. Please choose another date.';
        }
    }

    function validateDates() {
        const fromDate = fromDateInput.value;
        const toDate = toDateInput.value;

        if (fromDate && toDate) {
            if (new Date(fromDate) > new Date(toDate)) {
                alert('To Date must be after From Date.');
                return false;
            }

            if (isWeekend(fromDate) || isBankHoliday(fromDate) || isWeekend(toDate) || isBankHoliday(toDate)) {
                alert('Leave dates cannot be on weekends or bank holidays.');
                return false;
            }
        }

        return true;
    }

    // Set minimum date for date inputs to today
    if (fromDateInput && toDateInput) {
        fromDateInput.min = today;
        toDateInput.min = today;

        // Ensure 'To Date' is not before 'From Date'
        fromDateInput.addEventListener('change', function () {
            toDateInput.min = this.value;
            updateDateValidationMessages();
        });

        toDateInput.addEventListener('change', updateDateValidationMessages);
    }

    if (leaveRequestForm) {
        leaveRequestForm.addEventListener('submit', async (event) => {
            if (!validateDates()) {
                event.preventDefault();
                return;
            }

            event.preventDefault();
            const formData = new FormData(event.target);
            const data = Object.fromEntries(formData);
            console.log(data);

            try {
                const response = await fetch("http://localhost:8088/LeaveManagement/leaveRequest", {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    window.location.href = 'success.html';
                } else {
                    console.error('Error submitting leave request:', response.statusText);
                    alert('There was an error submitting your leave request. Please try again.');
                }
            } catch (error) {
                console.error('Error:', error);
                alert('There was an error submitting your leave request. Please try again.');
            }
        });
    }

    // Function to create leave request item
    function createLeaveRequestItem(leave) {
        return `
            <div class="leave-request-item">
                <div class="leave-info">
                    <h3>${leave.leaveType}</h3>
                    <p><strong>From:</strong> ${leave.fromDate}</p>
                    <p><strong>To:</strong> ${leave.toDate}</p>
                    <p><strong>Status:</strong> <span class="status status-${leave.status.toLowerCase()}">${leave.status}</span></p>
                </div>
            </div>
        `;
    }

    // Function to fetch and display leave requests
    async function fetchLeaveRequests() {
        console.log("Fetching leave requests...");
        try {
            const response = await fetch('http://localhost:8088/LeaveManagement/fetchLeaveRequests', {
                method: 'GET'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            if (leaveRequestsContainer) {
                leaveRequestsContainer.innerHTML = data.length > 0
                    ? data.map(createLeaveRequestItem).join('')
                    : '<p>No leave requests available.</p>';
            }
        } catch (error) {
            console.error('Error fetching leave requests:', error);
            if (leaveRequestsContainer) {
                leaveRequestsContainer.innerHTML = '<p>Error loading leave requests. Please try again later.</p>';
            }
        }
    }

    // Function to fetch and display team leaves
    const managerId = '1';
    async function fetchTeamLeaves() {
        try {
            const response = await fetch(`http://localhost:8088/LeaveManagement/fetchTeamLeaves?managerId=${managerId}`, {
                method: 'GET'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            if (teamLeavesMessage) {
                teamLeavesMessage.innerHTML = data.length > 0
                    ? data.map(createLeaveRequestItem).join('') : 'No team leave information available.';
            }
        } catch (error) {
            console.error('Error fetching team leaves:', error);
            if (teamLeavesMessage) {
                teamLeavesMessage.innerHTML = 'Error loading team leaves. Please try again later.';
            }
        }
    }

    // Function to handle user login
    async function handleLogin(email, password) {
        try {
            const response = await fetch('/loginCredentials', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                throw new Error('Invalid credentials');
            }

            const { employeeName, isManager } = await response.json();

            if (profileIcon) {
                profileIcon.innerText = employeeName.charAt(0).toUpperCase();
            }

            document.getElementById('login-section').style.display = 'none';
            document.getElementById('dashboard').style.display = 'block';

            await fetchLeaveRequests();

            if (isManager) {
                if (teamLeavesSection) {
                    teamLeavesSection.style.display = 'block';
                }
                await fetchTeamLeaves();
            }
        } catch (error) {
            console.error('Login failed:', error);
            const loginError = document.getElementById('login-error');
            if (loginError) {
                loginError.innerText = error.message;
            }
        }
    }

    // Handle login form submission
    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            await handleLogin(email, password);
        });
    } else {
        console.error('Login form element not found');
    }

    // Handle logout
    document.querySelectorAll('.dropdown a[href="logout.html"]').forEach(link => {
        link.addEventListener('click', () => {
            document.getElementById('dashboard').style.display = 'none';
            document.getElementById('login-section').style.display = 'block';
        });
    });

    // Add event listeners for fetch buttons
    if (fetchLeaveRequestsBtn) {
        fetchLeaveRequestsBtn.addEventListener('click', () => {
            fetchLeaveRequests();
        });
    }

    if (fetchTeamLeavesBtn) {
        fetchTeamLeavesBtn.addEventListener('click', () => {
            fetchTeamLeaves();
        });
    }

    // Call the function to fetch leave requests when the page loads
    fetchLeaveRequests().catch(error => {
        console.error('Error initializing leave requests:', error);
    });
});
