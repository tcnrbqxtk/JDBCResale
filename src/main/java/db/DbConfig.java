package db;

public class DbConfig {
    // Константы для подключения к PostgreSQL
    public static final String JDBC_URL  = "jdbc:postgresql://localhost:5432/postgres";
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "postgres";

    // Настройки пула соединений HikariCP
    public static final int MAX_POOL_SIZE = 10;
    public static final int MIN_IDLE = 2;
    public static final long CONNECTION_TIMEOUT_MS = 30000;
}



