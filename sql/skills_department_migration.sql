-- Non-destructive migration for an existing smart_hospital database.
-- The application bootstrap additionally adds sensible default mappings for built-in skills.
USE smart_hospital;

CREATE TABLE IF NOT EXISTS skill_departments (
  skill_id INT NOT NULL,
  department_id INT NOT NULL,
  PRIMARY KEY (skill_id, department_id),
  CONSTRAINT fk_skill_departments_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE,
  CONSTRAINT fk_skill_departments_department FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE CASCADE
);

-- Preserve compatibility: map existing skills to departments where they are already used.
INSERT IGNORE INTO skill_departments(skill_id, department_id)
SELECT ss.skill_id, st.department_id
FROM staff_skills ss JOIN staff st ON st.staff_id = ss.staff_id;

INSERT IGNORE INTO skill_departments(skill_id, department_id)
SELECT dr.skill_id, d.department_id
FROM duty_required_skills dr JOIN duties d ON d.duty_id = dr.duty_id;
