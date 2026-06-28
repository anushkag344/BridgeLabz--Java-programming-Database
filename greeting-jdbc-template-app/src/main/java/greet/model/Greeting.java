package greet.model;

public class Greeting {

    private int id;
    private String message;
    private int createdBy;
    private String creatorName;

    public Greeting() {
    }

    public Greeting(int id,
                    String message,
                    int createdBy,
                    String creatorName) {

        this.id = id;
        this.message = message;
        this.createdBy = createdBy;
        this.creatorName = creatorName;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public String getCreatorName() {
        return creatorName;
    }
}