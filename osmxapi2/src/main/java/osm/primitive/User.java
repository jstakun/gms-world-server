package osm.primitive;

public class User {

    public User(int id, String user) {
       this.id = id;
       this.name = user;
    }

    private int id;
    private String name;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
