package service;

import db.ConnectionPool;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Transaction {
    public static void main(String[] args) {
        System.out.println("\n--- Транзакция покупки ---");
        Scanner scanner = new Scanner(System.in);

        // Тестовые данные
        int buyerId = 3;
        int lotIdForTest = 144;
        int expertIdForTest = 1;
        BigDecimal fee = new BigDecimal("1500.00");

        String checkLotSql = "SELECT status, price FROM marketplace.lots WHERE id = ?";
        String updateLotSql = "UPDATE marketplace.lots SET status = 'sold' WHERE id = ?";
        String insertOrderSql = "INSERT INTO marketplace.orders " +
                "(final_price, service_fee, lot_id, buyer_id) " +
                "VALUES (?, ?, ?, ?)";
        String insertVerificationSql = "INSERT INTO marketplace.authenticity_checks " +
                "(check_date, result, lot_id, expert_id) " +
                "VALUES (CURRENT_TIMESTAMP, 'In process', ?, ?)";

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal finalPrice = null;

                // ШАГ 1: Проверяем статус лота
                try (PreparedStatement ps = conn.prepareStatement(checkLotSql)) {
                    ps.setInt(1, lotIdForTest);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String status = rs.getString("status");
                            if (!"active".equals(status)) {
                                throw new SQLException("Лот уже продан или недоступен!");
                            }
                            finalPrice = rs.getBigDecimal("price");
                        } else {
                            throw new SQLException("Лот с ID " + lotIdForTest + " не найден!");
                        }
                    }
                }
                System.out.println("   -> Шаг 1: Лот проверен. Автоматически считана цена из БД: " + finalPrice);

                // ШАГ 2: Меняем статус лота на 'sold'
                try (PreparedStatement ps = conn.prepareStatement(updateLotSql)) {
                    ps.setInt(1, lotIdForTest);
                    ps.executeUpdate();
                }
                System.out.println("   -> Шаг 2: Статус лота в памяти изменен на 'sold'");

                // ШАГ 3: Создаем заказ
                int orderId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setBigDecimal(1, finalPrice);
                    ps.setBigDecimal(2, fee);
                    ps.setInt(3, lotIdForTest);
                    ps.setInt(4, buyerId);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            orderId = keys.getInt(1);
                        }
                    }
                }
                System.out.println("   -> Шаг 3: Заказ подготовлен в буфере. ID нового заказа: " + orderId);

                // ШАГ 4: Направляем лот эксперту на верификацию в таблицу autenticity_checks
                try (PreparedStatement ps = conn.prepareStatement(insertVerificationSql)) {
                    ps.setInt(1, lotIdForTest);
                    ps.setInt(2, expertIdForTest);
                    ps.executeUpdate();
                }
                System.out.println("   -> Шаг 4: Пара автоматически направлена на аутентификацию в таблицу autenticity_checks");

                System.out.print("\n❓ Закоммитить изменения в БД? (yes/no): ");
                String choice = scanner.nextLine().trim().toLowerCase();

                if ("yes".equals(choice) || "y".equals(choice)) {
                    conn.commit();
                    System.out.println("\n   ✅ [COMMIT] Транзакция успешно завершена! Данные сохранены.");
                } else {
                    conn.rollback();
                    System.out.println("\n   ❌ [ROLLBACK] Транзакция отменена пользователем.");
                }

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("\n   ❌ [ROLLBACK] Произошла ошибка. Все изменения откачены назад!");
                System.err.println("   Причина: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Ошибка подключения к пулу БД: " + e.getMessage());
        } finally {
            ConnectionPool.close();
        }
    }
}