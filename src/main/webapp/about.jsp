<%@ page contentType="text/html;charset=UTF-8" %>
<%
    request.setAttribute("pageTitle", "About DutySphere");
%>
<%@ include file="WEB-INF/header.jsp" %>

<style>
    .about-hero {
        background: linear-gradient(130deg, #142d4e, #216b80);
        color: #fff;
        border-radius: 22px;
        padding: 30px;
    }

    .about-hero p {
        max-width: 850px;
        color: #e0edf6;
    }

    .about-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        gap: 16px;
    }

    .about-tile {
        height: 100%;
        border: 1px solid #e5eaf0;
        border-radius: 16px;
        padding: 20px;
        background: #fff;
    }

    .about-tile i {
        font-size: 22px;
        color: #247c91;
        margin-bottom: 12px;
    }

    .about-tile h5 {
        font-weight: 700;
    }

    .about-section {
        margin-top: 22px;
    }

    .about-list li {
        margin-bottom: 8px;
    }
</style>

<div class="about-hero mb-4">
    <div class="eyebrow" style="color:#b8dce8">SYSTEM OVERVIEW</div>
    <h2 class="fw-bold mt-2">DutySphere</h2>
    <p class="mb-0">
        Smart Hospital Ground Staff Management and Duty Allocation System.
        A role-based workspace for coordinating hospital ground staff,
        allocating duties fairly, tracking attendance and leave, and
        verifying completed work.
    </p>
</div>

<div class="about-section">
    <h4 class="mb-3">Aim</h4>
    <div class="cardx pad">
        To provide a centralized, reliable and workload-aware system for
        managing hospital ground staff and duty allocation, improving
        coordination, transparency and timely completion of hospital operations.
    </div>
</div>

<div class="about-section">
    <h4 class="mb-3">Objectives</h4>

    <div class="about-grid">
        <div class="about-tile">
            <i class="fa-solid fa-calendar-check"></i>
            <h5>Efficient duty allocation</h5>
            <p class="mini">
                Match eligible staff to duties using role, department, skills,
                availability and workload.
            </p>
        </div>

        <div class="about-tile">
            <i class="fa-solid fa-scale-balanced"></i>
            <h5>Balanced workload</h5>
            <p class="mini">
                Support fair allocation and help managers identify excessive workload.
            </p>
        </div>

        <div class="about-tile">
            <i class="fa-solid fa-bell"></i>
            <h5>Timely communication</h5>
            <p class="mini">
                Provide in-app notifications and configured email alerts for
                important workflow events.
            </p>
        </div>

        <div class="about-tile">
            <i class="fa-solid fa-clipboard-check"></i>
            <h5>Accountability</h5>
            <p class="mini">
                Track attendance, leave, duty progress, change requests and
                proof verification.
            </p>
        </div>
    </div>
</div>

<div class="about-section">
    <h4 class="mb-3">Key Features</h4>

    <div class="cardx pad">
        <ul class="about-list">
            <li>Role-based access for Administrator, Supervisor and Ground Staff.</li>
            <li>Department, supervisor and staff management.</li>
            <li>Smart duty allocation with eligibility checks and workload-aware recommendations.</li>
            <li>Duty acknowledgement, progress updates and change requests.</li>
            <li>Attendance check-in and check-out tracking.</li>
            <li>Leave requests and approval/rejection workflow.</li>
            <li>Work-proof upload, review, verification and rejection.</li>
            <li>In-app notification centre with read/unread status.</li>
            <li>Email notifications through the configured PHP SMTP service.</li>
            <li>Reports, audit logging and responsive desktop/mobile interface.</li>
            <li>Administrator profile and account settings.</li>
        </ul>
    </div>
</div>

<div class="about-section">
    <h4 class="mb-3">Technology Stack</h4>

    <div class="about-grid">
        <div class="about-tile">
            <h5>Frontend</h5>
            <p class="mini mb-0">
                JSP, HTML, CSS, JavaScript and Bootstrap
            </p>
        </div>

        <div class="about-tile">
            <h5>Backend</h5>
            <p class="mini mb-0">
                Java Servlets, JDBC and Apache Tomcat 9
            </p>
        </div>

        <div class="about-tile">
            <h5>Database</h5>
            <p class="mini mb-0">
                MySQL / MariaDB
            </p>
        </div>

        <div class="about-tile">
            <h5>Notification service</h5>
            <p class="mini mb-0">
                PHP service with configurable SMTP email delivery
            </p>
        </div>
    </div>
</div>

<div class="about-section">
    <h4 class="mb-3">User Roles</h4>

    <div class="cardx pad">
        <p>
            <b>Administrator:</b>
            manages system-wide workforce, departments, supervisors, duties,
            account settings and reports.
        </p>

        <p>
            <b>Supervisor:</b>
            manages duties and operational workflows within assigned
            department(s), reviews work proofs and responds to requests.
        </p>

        <p class="mb-0">
            <b>Ground Staff:</b>
            views assigned duties, records attendance, requests leave or
            duty changes, and submits work proof.
        </p>
    </div>
</div>

<%@ include file="WEB-INF/footer.jsp" %>