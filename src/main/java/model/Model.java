package model;

public class Model {
    private int id;
    private String brand;
    private String line;
    private String color;

    public Model(String brand, String line, String color) {
        this.brand = brand;
        this.line = line;
        this.color = color;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBrand() { return brand; }
    public String getLine() { return line; }
    public String getColor() { return color; }
}
