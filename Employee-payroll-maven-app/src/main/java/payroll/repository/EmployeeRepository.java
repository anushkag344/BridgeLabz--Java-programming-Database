package payroll.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;
import payroll.model.Employee;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class EmployeeRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * RowMapper
     */
    private final RowMapper<Employee> employeeRowMapper = (ResultSet rs, int rowNum) -> {

        Employee emp = new Employee();

        emp.setId(rs.getInt("id"));
        emp.setName(rs.getString("name"));
        emp.setProfileImage(rs.getString("profile_image"));
        emp.setGender(rs.getString("gender"));

        String deptString = rs.getString("departments");

        if (deptString != null && !deptString.isEmpty()) {
            emp.setDepartments(
                    Arrays.stream(deptString.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList())
            );
        }

        emp.setSalary(rs.getBigDecimal("salary"));

        if (rs.getDate("start_date") != null) {
            emp.setStartDate(rs.getDate("start_date").toLocalDate());
        }

        emp.setNotes(rs.getString("notes"));
        emp.setCreatedBy(rs.getInt("created_by"));

        return emp;
    };

    // =========================
    // ADD EMPLOYEE
    // =========================
    public void addEmployee(Employee emp) {

        transactionTemplate.execute(status -> {

            String sql = """
                    INSERT INTO employees
                    (name, profile_image, gender, salary, start_date, notes, created_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    RETURNING id
                    """;

            Integer empId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    emp.getName(),
                    emp.getProfileImage(),
                    emp.getGender(),
                    emp.getSalary(),
                    emp.getStartDate(),
                    emp.getNotes(),
                    emp.getCreatedBy()
            );

            String deptSql = """
                    INSERT INTO employee_departments (employee_id, department)
                    VALUES (?, ?)
                    """;

            if (emp.getDepartments() != null) {
                for (String dept : emp.getDepartments()) {
                    jdbcTemplate.update(deptSql, empId, dept.trim());
                }
            }

            return null;
        });
    }

    // =========================
    // GET ALL
    // =========================
    public List<Employee> findAll() {

        String sql = """
                SELECT
                    e.id,
                    e.name,
                    e.profile_image,
                    e.gender,
                    STRING_AGG(ed.department, ',') AS departments,
                    e.salary,
                    e.start_date,
                    e.notes,
                    e.created_by
                FROM employees e
                LEFT JOIN employee_departments ed
                    ON e.id = ed.employee_id
                GROUP BY e.id
                ORDER BY e.id DESC
                """;

        return jdbcTemplate.query(sql, employeeRowMapper);
    }

    // =========================
    // FIND BY ID
    // =========================
    public Employee findById(int id) {

        String sql = """
                SELECT
                    e.id,
                    e.name,
                    e.profile_image,
                    e.gender,
                    STRING_AGG(ed.department, ',') AS departments,
                    e.salary,
                    e.start_date,
                    e.notes,
                    e.created_by
                FROM employees e
                LEFT JOIN employee_departments ed
                    ON e.id = ed.employee_id
                WHERE e.id = ?
                GROUP BY e.id
                """;

        try {
            return jdbcTemplate.queryForObject(sql, employeeRowMapper, id);
        } catch (Exception e) {
            return null;
        }
    }

    // =========================
    // FIND BY EMAIL
    // =========================
    public Employee findByEmail(String email) {

        String sql = """
                SELECT
                    e.id,
                    e.name,
                    e.profile_image,
                    e.gender,
                    STRING_AGG(ed.department, ',') AS departments,
                    e.salary,
                    e.start_date,
                    e.notes,
                    e.created_by
                FROM employees e
                LEFT JOIN employee_departments ed
                    ON e.id = ed.employee_id
                LEFT JOIN users u
                    ON e.created_by = u.id
                WHERE u.email = ?
                GROUP BY e.id
                """;

        try {
            return jdbcTemplate.queryForObject(sql, employeeRowMapper, email);
        } catch (Exception e) {
            return null;
        }
    }

    // =========================
    // UPDATE
    // =========================
    public void updateEmployee(int id, BigDecimal salary, String notes, int adminId) {

        String sql = """
                UPDATE employees
                SET salary = ?,
                    notes = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, salary, notes, id);
    }

    // =========================
    // DELETE
    // =========================
    public void deleteEmployee(int id) {

        jdbcTemplate.update("DELETE FROM employees WHERE id = ?", id);
    }

    // =========================
    // DEPT PAYROLL
    // =========================
    public BigDecimal getDeptPayroll(String dept) {

        String sql = """
                SELECT COALESCE(SUM(e.salary), 0)
                FROM employees e
                INNER JOIN employee_departments ed
                    ON e.id = ed.employee_id
                WHERE ed.department = ?
                """;

        return jdbcTemplate.queryForObject(sql, BigDecimal.class, dept);
    }

    // =========================
    // AUDIT LOGS
    // =========================
    public List<Map<String, Object>> findAuditLogs() {

        return jdbcTemplate.queryForList(
                "SELECT * FROM payroll_audit ORDER BY changed_at DESC"
        );
    }
}