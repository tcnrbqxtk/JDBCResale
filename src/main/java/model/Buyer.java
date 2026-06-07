package model;

import java.math.BigDecimal;

public class Buyer {
    private int id;
    private String fullName;
    private BigDecimal rating; // Для NUMERIC в Java всегда используем BigDecimal

    // Конструктор №1: для создания НОВОГО покупателя (БД сама сгенерирует ID)
    public Buyer(String fullName, BigDecimal rating) {
        this.fullName = fullName;
        this.rating = rating;
    }

    // Конструктор №2: для чтения из базы (когда ID уже прилетел из SQL)
    public Buyer(int id, String fullName, BigDecimal rating) {
        this.id = id;
        this.fullName = fullName;
        this.rating = rating;
    }

    // Геттеры и сеттеры (сгенерируй их автоматически через Alt+Insert в IDEA)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public BigDecimal getRating() { return rating; }
}
