<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%
    request.setAttribute("pageTitle", "Skills Management");

    Map<String, Object> edit =
        (Map<String, Object>) request.getAttribute("editSkill");

    Set<Integer> selected =
        (Set<Integer>) request.getAttribute("selectedDepartments");

    if (selected == null) {
        selected = Collections.emptySet();
    }
%>
<%@ include file="WEB-INF/header.jsp" %>

<div class="cardx pad mb-3">
    <div class="toolbar">
        <div>
            <b>Skills catalogue</b>
            <div class="mini">
                Create skills and link each skill to one or more departments.
                Staff and duties can select only skills mapped to their chosen department.
            </div>
        </div>

        <% if (edit != null) { %>
            <a class="btn-soft" href="<%= request.getContextPath() %>/skills">
                Cancel edit
            </a>
        <% } %>
    </div>

    <form method="post" action="<%= request.getContextPath() %>/skills" class="mt-3">
        <input type="hidden" name="action" value="save">
        <input
            type="hidden"
            name="skill_id"
            value="<%= edit == null ? "" : edit.get("id") %>"
        >

        <div class="row g-3">
            <div class="col-md-4">
                <label class="form-label">Skill name</label>
                <input
                    class="form-control"
                    name="skill_name"
                    maxlength="100"
                    required
                    value="<%= edit == null ? "" : String.valueOf(edit.get("name")).replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;") %>"
                    placeholder="e.g. Wheelchair Handling"
                >
            </div>

            <div class="col-md-8">
                <label class="form-label">Description</label>
                <input
                    class="form-control"
                    name="description"
                    maxlength="255"
                    value="<%= edit == null || edit.get("description") == null ? "" : String.valueOf(edit.get("description")).replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;") %>"
                    placeholder="Describe the capability"
                >
            </div>

            <div class="col-12">
                <label class="form-label">Departments that use this skill</label>

                <div class="row g-2">
                    <% for (Map<String, Object> d :
                        (List<Map<String, Object>>) request.getAttribute("departments")) {
                        int did = ((Number) d.get("id")).intValue();
                    %>
                        <div class="col-sm-6 col-md-4 col-lg-3">
                            <label class="mini">
                                <input
                                    type="checkbox"
                                    name="department_id"
                                    value="<%= did %>"
                                    <%= selected.contains(did) ? "checked" : "" %>
                                >
                                <%= d.get("name") %>
                            </label>
                        </div>
                    <% } %>
                </div>

                <div class="mini mt-2">
                    Select at least one department. A skill may be shared across departments when appropriate.
                </div>
            </div>

            <div class="col-12">
                <button class="btn-main" type="submit">
                    <%= edit == null ? "Add skill" : "Save skill changes" %>
                </button>
            </div>
        </div>
    </form>
</div>

<div class="cardx pad">
    <div class="toolbar">
        <div>
            <b>Configured skills</b>
            <div class="mini">
                Department mappings used by Staff Directory and Duty Control
            </div>
        </div>

        <input
            id="skillSearch"
            class="form-control"
            style="max-width:240px"
            onkeyup="filterTable('skillSearch','skillsTable')"
            placeholder="Search skills"
        >
    </div>

    <div class="table-responsive">
        <table class="table" id="skillsTable">
            <thead>
                <tr>
                    <th>Skill</th>
                    <th>Description</th>
                    <th>Departments</th>
                    <th>Actions</th>
                </tr>
            </thead>

            <tbody>
                <% for (Map<String, Object> s :
                    (List<Map<String, Object>>) request.getAttribute("skills")) {
                %>
                    <tr>
                        <td>
                            <b><%= s.get("name") %></b>
                        </td>

                        <td>
                            <%= s.get("description") == null ? "—" : s.get("description") %>
                        </td>

                        <td><%= s.get("departmentNames") %></td>

                        <td>
                            <div class="d-flex gap-1">
                                <a
                                    class="btn-soft"
                                    href="<%= request.getContextPath() %>/skills?edit=<%= s.get("id") %>"
                                >
                                    Edit
                                </a>

                                <form
                                    method="post"
                                    action="<%= request.getContextPath() %>/skills"
                                    onsubmit="return confirm('Delete this unused skill?')"
                                >
                                    <input type="hidden" name="action" value="delete">
                                    <input
                                        type="hidden"
                                        name="skill_id"
                                        value="<%= s.get("id") %>"
                                    >

                                    <button class="btn-soft" type="submit">
                                        Delete
                                    </button>
                                </form>
                            </div>
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="WEB-INF/footer.jsp" %>