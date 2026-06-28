package payroll.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Employee {

    private int id;
    private String name;
    private String profileImage;
    private String gender;
    private List<String> departments;
    private BigDecimal salary;
    private LocalDate startDate;
    private String notes;
    private int createdBy;

    // Default Constructor
    public Employee() {
    }

    // Parameterized Constructor
    public Employee(int id, String name, String profileImage,
                    String gender, List<String> departments,
                    BigDecimal salary, LocalDate startDate,
                    String notes, int createdBy) {

        this.id = id;
        this.name = name;
        this.profileImage = profileImage;
        this.gender = gender;
        this.departments = departments;
        this.salary = salary;
        this.startDate = startDate;
        this.notes = notes;
        this.createdBy = createdBy;
    }

    // Getter & Setter for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter & Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter & Setter for profileImage
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // Getter & Setter for gender
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    // Getter & Setter for departments
    public List<String> getDepartments() {
        return departments;
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments;
    }

    // Getter & Setter for salary
    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    // Getter & Setter for startDate
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    // Getter & Setter for notes
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Getter & Setter for createdBy
    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    // toString()
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", gender='" + gender + '\'' +
                ", departments=" + departments +
                ", salary=" + salary +
                ", startDate=" + startDate +
                ", notes='" + notes + '\'' +
                ", createdBy=" + createdBy +
                '}';
    }
}