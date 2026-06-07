package dao;

import db.ConnectionPool;
import model.Model;
import java.sql.*;

public class ModelDao {
    // Вставляем модель и возвращаем сгенерированный ID (Аналог HallDao.insert())
    public int insert(Model model) throws SQLException {
        String sql = "INSERT INTO marketplace.models (brand, line, color) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, model.getBrand());
            ps.setString(2, model.getLine());
            ps.setString(3, model.getColor());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    model.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось сохранить модель кроссовок");
        }
    }
}
