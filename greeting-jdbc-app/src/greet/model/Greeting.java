package greet.model;
public class Greeting {
    private int id;
    private String message;
    private int createdBy;
    private String creatorName;
    public Greeting() {}
    public Greeting(int id, String message, int createdBy, String creatorName) {
        this.id = id;
        this.message = message;
        this.createdBy = createdBy;
        this.creatorName = creatorName;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public int getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    public String getCreatorName() {
        return creatorName;
    }
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName; }
    @Override
    public String toString() {
        return String.format("[%d] Message: \"%s\" (Posted by: %s)", id, message,
                creatorName);
    }
}