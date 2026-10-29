# Exact end-to-end verification scenario

## Goal
Verify that Smart Allocation can recommend Ramesh Patil for the ICU Ground Support Demonstration duty and that the assignment reaches the staff dashboard.

## Expected data
- Ramesh Patil: STF-104
- Department: ICU
- Role: ICU Attendant
- Experience: 4 years
- Login: ramesh101 / ramesh@123
- Verified skill: Patient Handling
- Demo duty: ICU Ground Support Demonstration
- Duty date: tomorrow relative to application startup
- Window: 09:00-13:00
- Priority: HIGH
- Required skill: Patient Handling

## Test sequence
1. Log in as `admin / admin@123`.
2. Open Staff Directory.
3. Confirm Ramesh Patil is ACTIVE, ICU, ICU Attendant, and has Manage action.
4. Click Manage. Confirm profile, skills and login fields are editable.
5. Open Duty Control.
6. Find ICU Ground Support Demonstration.
7. Click Recommend.
8. Ramesh must appear as an eligible candidate if he has no conflicting duty/approved leave.
9. Confirm the recommendation shows skill, workload, experience, fairness and suitability score.
10. Click Assign this staff member.
11. Confirm duty status becomes ASSIGNED and Ramesh receives an in-app notification.
12. Log out.
13. Log in as `ramesh101 / ramesh@123`.
14. Dashboard must show Upcoming assignments = 1 and the Next assigned duty card.
15. Open My Duties. The ICU duty must be listed.
16. Acknowledge the duty, then Start Work.
17. Upload work proof from Work Proof.
18. Log in as ICU supervisor.
19. Open Work Proof and verify the submitted proof. Supervisor must not see an upload control.

## If Recommend shows an error
The new AllocationServlet returns the actual server-side error as JSON instead of silently returning an empty list. Open the browser developer console/network response for `/allocation?duty_id=...` and inspect the error.

## Database checks
```sql
USE smart_hospital;
SELECT user_id,username,role,status,full_name FROM users;
SELECT s.staff_code,s.name,d.department_name,r.role_name,s.experience_years,s.status
FROM staff s JOIN departments d ON s.department_id=d.department_id JOIN roles r ON s.role_id=r.role_id
WHERE s.staff_code='STF-104';
SELECT sk.skill_name,ss.verification_status
FROM staff_skills ss JOIN skills sk ON sk.skill_id=ss.skill_id
JOIN staff s ON s.staff_id=ss.staff_id WHERE s.staff_code='STF-104';
SELECT d.duty_id,d.duty_name,d.duty_date,d.start_time,d.end_time,d.priority,d.status
FROM duties d WHERE d.duty_name='ICU Ground Support Demonstration';
SELECT d.duty_id,sk.skill_name
FROM duty_required_skills dr JOIN duties d ON d.duty_id=dr.duty_id JOIN skills sk ON sk.skill_id=dr.skill_id
WHERE d.duty_name='ICU Ground Support Demonstration';
```

## Staff duty completion + proof verification
1. Log in as `ramesh101` / `ramesh@123`.
2. Open **My Duties** and use **Acknowledge**.
3. Use **Start work**. The duty becomes `IN_PROGRESS`.
4. Click **Mark completed**. The assignment becomes `COMPLETION_PENDING`.
5. Click **Submit proof** and upload JPG/JPEG/PNG/PDF.
6. Log in as Admin or the mapped Supervisor and open **Work Proof**.
7. Click **Work done** to verify the proof. The assignment becomes `COMPLETED`.
8. If the proof is rejected, the assignment returns to `IN_PROGRESS`, allowing the staff member to correct and resubmit.

## Notification regression checks
1. Assign a duty to an active staff member: the staff member should see `DUTY_ASSIGNED` in Notifications and receive an email if SMTP is configured.
2. Submit a leave request: assigned supervisors and active administrators should see `LEAVE_REQUEST`; configured email recipients should also receive it.
3. Approve/reject a leave request: the requesting staff member should see `LEAVE_RESPONSE` and receive an email when configured.
4. Submit a duty-change request: assigned supervisors and administrators should see `CHANGE_REQUEST` and receive email when configured.
5. Approve/reject a duty-change request: the staff member should see `CHANGE_REQUEST_RESPONSE` and receive email when configured.
6. Submit work proof: relevant supervisors and administrators should see `WORK_PROOF_SUBMITTED` and receive email when configured.
7. Verify/reject work proof: the staff member should see `WORK_PROOF_REVIEW` and receive email when configured.
8. Check in/check out: the staff member should see an `ATTENDANCE_CHECKIN` or `ATTENDANCE_CHECKOUT` notification and receive email when configured.
9. Open About from the navigation: the DutySphere aim, objectives, features, technology stack and user roles should be visible.

Email checks require a valid PHP service URL and SMTP configuration. In-app notifications do not depend on SMTP.


## Skills catalogue and department-aware skill selection
1. Log in as `admin / admin@123` and open **Skills Catalogue**.
2. Create a skill and select one or more departments; verify it appears in the catalogue with those mappings.
3. Open Staff Directory and click Add staff. Select a department and verify that only its mapped skills are selectable. Change department and verify invalid skill checkboxes are unchecked and disabled.
4. Save the staff record with a valid skill selection. Attempt a crafted POST containing a skill not mapped to the selected department; the server must reject the request.
5. Open Duty Control and create a duty. Required skills must be filtered by the selected department, and invalid cross-department skill IDs must be rejected server-side.
6. Click Recommend and verify candidates missing a required verified skill are excluded.
7. Attempt to remove a department mapping from a skill currently assigned to staff or required by a duty in that department; the application must reject the change rather than silently breaking eligibility.
8. Attempt to delete a skill used by staff or duties; the application must prevent deletion.
9. Create a new department, add a new skill mapped to it, then verify the skill is selectable for staff and duties in that department.
