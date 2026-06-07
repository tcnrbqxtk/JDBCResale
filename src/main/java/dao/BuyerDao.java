package dao;

import db.ConnectionPool;
import db.ConnectionPool;
import model.Buyer;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BuyerDao {

    // 📥 1. CREATE — Добавление нового покупателя в базу
    public int insert(Buyer buyer) throws SQLException {
        String sql = "INSERT INTO marketplace.buyers (full_name, rating) VALUES (?, ?)";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, buyer.getFullName());
            ps.setBigDecimal(2, buyer.getRating());
            ps.executeUpdate();

            // Забираем ID, который сгенерировала БД
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    buyer.setId(generatedId);
                    return generatedId;
                }
            }
            throw new SQLException("Не удалось получить ID покупателя");
        }
    }

    // 🔍 2. READ ALL — Получение списка всех покупателей из базы
    public List<Buyer> findAll() throws SQLException {
        String sql = "SELECT id, full_name, rating FROM marketplace.buyers ORDER BY id";
        List<Buyer> buyers = new ArrayList<>();

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                buyers.add(new Buyer(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getBigDecimal("rating")
                ));
            }
        }
        return buyers;
    }

    // 🔄 3. UPDATE — Обновление данных существующего покупателя
    public boolean update(Buyer buyer) throws SQLException {
        String sql = "UPDATE marketplace.buyers SET full_name = ?, rating = ? WHERE id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, buyer.getFullName());
            ps.setBigDecimal(2, buyer.getRating());
            ps.setInt(3, buyer.getId()); // Ищем по ID, кого именно обновлять

            // executeUpdate() возвращает количество измененных строк. Если больше 0 — успех.
            return ps.executeUpdate() > 0;
        }
    }

    // ❌ 4. DELETE — Удаление покупателя по его ID
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM marketplace.buyers WHERE id = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // 🛒 5. ТРАНЗАКЦИЯ — Безопасное оформление заказа на покупку кроссовок (лота)
    public int placeOrder(int buyerId, int lotId, BigDecimal serviceFee) throws SQLException {
        // Выбираем и статус, и цену лота по его ID
        String checkLotSql = "SELECT status, price FROM marketplace.lots WHERE id = ?";
        String updateLotSql = "UPDATE marketplace.lots SET status = 'sold' WHERE id = ?";
        String insertOrderSql = "INSERT INTO marketplace.orders (final_price, service_fee, lot_id, buyer_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false); // Стартуем транзакцию вручную

            try {
                BigDecimal finalPrice = null;

                // ШАГ 1: Проверяем статус лота и ЗАБИРАЕМ ЕГО ЦЕНУ ИЗ БАЗЫ
                try (PreparedStatement ps = conn.prepareStatement(checkLotSql)) {
                    ps.setInt(1, lotId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String status = rs.getString("status");
                            if (!"active".equals(status)) {
                                throw new SQLException("Лот уже продан или недоступен!");
                            }
                            // Вот здесь мы считываем цену, которую ты видишь в DBeaver
                            finalPrice = rs.getBigDecimal("price");
                        } else {
                            throw new SQLException("Лот с таким ID не найден!");
                        }
                    }
                }

                // ШАГ 2: Меняем статус лота на 'sold'
                try (PreparedStatement ps = conn.prepareStatement(updateLotSql)) {
                    ps.setInt(1, lotId);
                    ps.executeUpdate();
                }

                // ШАГ 3: Создаем заказ (подставляя считанную цену finalPrice)
                int orderId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setBigDecimal(1, finalPrice); // Цена ушла из БД напрямую в заказ!
                    ps.setBigDecimal(2, serviceFee);
                    ps.setInt(3, lotId);
                    ps.setInt(4, buyerId);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            orderId = keys.getInt(1);
                        }
                    }
                }

                conn.commit(); // Фиксируем транзакцию
                return orderId;

            } catch (SQLException e) {
                conn.rollback(); // Откатываем всё в случае ошибки
                throw e;
            }
        }
    }
    public Optional<Buyer> findByFullName(String fullName) throws SQLException {
        String sql = "SELECT id, full_name, rating FROM marketplace.buyers WHERE full_name = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Buyer buyer = new Buyer(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getBigDecimal("rating")
                    );
                    return Optional.of(buyer); // Оборачиваем в Optional, если нашли
                }
            }
        }
        return Optional.empty(); // Возвращаем пустой Optional, если никого нет
    }
}
