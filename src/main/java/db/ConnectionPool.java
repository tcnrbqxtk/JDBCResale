package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DbConfig.JDBC_URL);
        config.setUsername(DbConfig.USERNAME);
        config.setPassword(DbConfig.PASSWORD);

        config.setMaximumPoolSize(DbConfig.MAX_POOL_SIZE);
        config.setMinimumIdle(DbConfig.MIN_IDLE);
        config.setConnectionTimeout(DbConfig.CONNECTION_TIMEOUT_MS);

        // Инициализируем сам пул соединений
        dataSource = new HikariDataSource(config);
    }

    // Этот метод теперь будут вызывать твои DAO и Сервисы вместо ConnectionPool.getConnection()
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Метод для закрытия пула при выходе из программы
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
