package com.smarthospital.controller;

import com.smarthospital.model.User;
import com.smarthospital.util.Access;
import com.smarthospital.util.Audit;
import com.smarthospital.util.DB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/** Administrator-only skill catalogue with department mappings. */
@WebServlet("/skills")
public class SkillsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("user");

        if (!Access.isAdmin(user)) {
            resp.sendError(403, "Only administrators can manage the skills catalogue.");
            return;
        }

        try (Connection c = DB.getConnection()) {
            List<Map<String, Object>> departments = new ArrayList<>();

            try (PreparedStatement p = c.prepareStatement(
                    "SELECT department_id,department_name FROM departments ORDER BY department_name");
                 ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", r.getInt(1));
                    m.put("name", r.getString(2));
                    departments.add(m);
                }
            }

            List<Map<String, Object>> skills = new ArrayList<>();

            String q = "SELECT s.skill_id,s.skill_name,s.description,COALESCE(GROUP_CONCAT(d.department_name ORDER BY d.department_name SEPARATOR ', '),'Not assigned'),COALESCE(GROUP_CONCAT(sd.department_id ORDER BY sd.department_id SEPARATOR ','),'') FROM skills s LEFT JOIN skill_departments sd ON sd.skill_id=s.skill_id LEFT JOIN departments d ON d.department_id=sd.department_id GROUP BY s.skill_id,s.skill_name,s.description ORDER BY s.skill_name";

            try (PreparedStatement p = c.prepareStatement(q);
                 ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", r.getInt(1));
                    m.put("name", r.getString(2));
                    m.put("description", r.getString(3));
                    m.put("departmentNames", r.getString(4));
                    m.put("departmentIds", r.getString(5));
                    skills.add(m);
                }
            }

            req.setAttribute("departments", departments);
            req.setAttribute("skills", skills);
            req.setAttribute("pageTitle", "Skills Management");

            String raw = req.getParameter("edit");

            if (raw != null && !raw.isBlank()) {
                int id = Integer.parseInt(raw);
                Map<String, Object> edit = null;
                Set<Integer> selected = new HashSet<>();

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT skill_id,skill_name,description FROM skills WHERE skill_id=?")) {

                    p.setInt(1, id);

                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) {
                            edit = new HashMap<>();
                            edit.put("id", r.getInt(1));
                            edit.put("name", r.getString(2));
                            edit.put("description", r.getString(3));
                        }
                    }
                }

                if (edit == null) {
                    resp.sendError(404, "Skill not found.");
                    return;
                }

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT department_id FROM skill_departments WHERE skill_id=?")) {

                    p.setInt(1, id);

                    try (ResultSet r = p.executeQuery()) {
                        while (r.next()) {
                            selected.add(r.getInt(1));
                        }
                    }
                }

                req.setAttribute("editSkill", edit);
                req.setAttribute("selectedDepartments", selected);
            }

            req.getRequestDispatcher("/skills.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(400, "Invalid skill ID.");
        } catch (Exception e) {
            throw new ServletException("Unable to load skills catalogue.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        User user = (User) req.getSession().getAttribute("user");

        if (!Access.isAdmin(user)) {
            resp.sendError(403, "Only administrators can manage the skills catalogue.");
            return;
        }

        req.setCharacterEncoding("UTF-8");

        String action = clean(req.getParameter("action"));

        try (Connection c = DB.getConnection()) {
            if ("save".equals(action)) {
                String raw = clean(req.getParameter("skill_id"));
                String name = clean(req.getParameter("skill_name"));
                String description = clean(req.getParameter("description"));
                String[] depts = req.getParameterValues("department_id");

                if (name.isBlank()) {
                    throw new IllegalArgumentException("Skill name is required.");
                }

                if (name.length() > 100 || description.length() > 255) {
                    throw new IllegalArgumentException("Skill name or description is too long.");
                }

                if (depts == null || depts.length == 0) {
                    throw new IllegalArgumentException(
                            "Select at least one department for this skill."
                    );
                }

                c.setAutoCommit(false);

                try {
                    int skillId;

                    if (raw.isBlank()) {
                        try (PreparedStatement p = c.prepareStatement(
                                "INSERT INTO skills(skill_name,description) VALUES(?,?)",
                                Statement.RETURN_GENERATED_KEYS)) {

                            p.setString(1, name);
                            p.setString(2, description.isBlank() ? null : description);
                            p.executeUpdate();

                            try (ResultSet r = p.getGeneratedKeys()) {
                                if (!r.next()) {
                                    throw new SQLException("Could not create skill.");
                                }

                                skillId = r.getInt(1);
                            }
                        }
                    } else {
                        skillId = Integer.parseInt(raw);

                        try (PreparedStatement p = c.prepareStatement(
                                "UPDATE skills SET skill_name=?,description=? WHERE skill_id=?")) {

                            p.setString(1, name);
                            p.setString(2, description.isBlank() ? null : description);
                            p.setInt(3, skillId);

                            if (p.executeUpdate() == 0) {
                                throw new IllegalArgumentException("Skill not found.");
                            }
                        }
                    }

                    Set<Integer> requestedDepartments = new HashSet<>();

                    for (String d : depts) {
                        requestedDepartments.add(Integer.parseInt(d));
                    }

                    String usageSql = "SELECT DISTINCT department_id FROM (SELECT st.department_id FROM staff_skills ss JOIN staff st ON st.staff_id=ss.staff_id WHERE ss.skill_id=? UNION SELECT du.department_id FROM duty_required_skills ds JOIN duties du ON du.duty_id=ds.duty_id WHERE ds.skill_id=?) used_departments";

                    try (PreparedStatement used = c.prepareStatement(usageSql)) {
                        used.setInt(1, skillId);
                        used.setInt(2, skillId);

                        try (ResultSet r = used.executeQuery()) {
                            while (r.next()) {
                                if (!requestedDepartments.contains(r.getInt(1))) {
                                    throw new IllegalArgumentException(
                                            "This skill is currently assigned to staff or duties in a department you removed. Update those assignments first."
                                    );
                                }
                            }
                        }
                    }

                    try (PreparedStatement p = c.prepareStatement(
                            "DELETE FROM skill_departments WHERE skill_id=?")) {

                        p.setInt(1, skillId);
                        p.executeUpdate();
                    }

                    try (PreparedStatement p = c.prepareStatement(
                            "INSERT INTO skill_departments(skill_id,department_id) VALUES(?,?)")) {

                        Set<Integer> unique = new HashSet<>();

                        for (String d : depts) {
                            int did = Integer.parseInt(d);

                            if (!unique.add(did)) {
                                continue;
                            }

                            try (PreparedStatement check = c.prepareStatement(
                                    "SELECT 1 FROM departments WHERE department_id=?")) {

                                check.setInt(1, did);

                                try (ResultSet r = check.executeQuery()) {
                                    if (!r.next()) {
                                        throw new IllegalArgumentException(
                                                "A selected department does not exist."
                                        );
                                    }
                                }
                            }

                            p.setInt(1, skillId);
                            p.setInt(2, did);
                            p.addBatch();
                        }

                        p.executeBatch();
                    }

                    Audit.log(
                            user.id(),
                            raw.isBlank() ? "SKILL_CREATED" : "SKILL_UPDATED",
                            name
                    );

                    c.commit();

                } catch (Exception e) {
                    c.rollback();
                    throw e;

                } finally {
                    c.setAutoCommit(true);
                }

            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("skill_id"));

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT skill_name,(SELECT COUNT(*) FROM staff_skills WHERE skill_id=?),(SELECT COUNT(*) FROM duty_required_skills WHERE skill_id=?) FROM skills WHERE skill_id=?")) {

                    p.setInt(1, id);
                    p.setInt(2, id);
                    p.setInt(3, id);

                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) {
                            throw new IllegalArgumentException("Skill not found.");
                        }

                        if (r.getInt(2) > 0 || r.getInt(3) > 0) {
                            throw new IllegalArgumentException(
                                    "This skill is already used by staff or duties. Remove its assignments first, or edit its department mapping instead."
                            );
                        }

                        String name = r.getString(1);

                        try (PreparedStatement del = c.prepareStatement(
                                "DELETE FROM skills WHERE skill_id=?")) {

                            del.setInt(1, id);
                            del.executeUpdate();
                        }

                        Audit.log(user.id(), "SKILL_DELETED", name);
                    }
                }

            } else {
                throw new IllegalArgumentException("Unsupported skills action.");
            }

            resp.sendRedirect(req.getContextPath() + "/skills?success=1");

        } catch (SQLIntegrityConstraintViolationException e) {
            resp.sendError(
                    400,
                    "A skill with that name already exists, or the skill is still in use."
            );

        } catch (Exception e) {
            resp.sendError(
                    400,
                    e.getMessage() == null
                            ? "Unable to save skills catalogue."
                            : e.getMessage()
            );
        }
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}