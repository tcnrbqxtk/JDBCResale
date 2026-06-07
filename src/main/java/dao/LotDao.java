package dao;

import db.ConnectionPool;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class LotDao {

    public void executeBatchLotInsertion(String brand, String line, String color, int sellerId, int sizeId, Scanner scanner) throws SQLException {
        String modelSql = "INSERT INTO marketplace.models (brand, line, color) VALUES (?, ?, ?) " +
                "ON CONFLICT (brand, line, color) DO UPDATE SET brand = EXCLUDED.brand RETURNING id";

        String linkSql = "INSERT INTO marketplace.model_sizes (model_id, size_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        String lotSql = "INSERT INTO marketplace.lots (price, status, seller_id, model_id, size_id) VALUES (?, 'active', ?, ?, ?)";

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int modelId;

                // 1. Безопасное получение/создание ID модели
                try (PreparedStatement ps = conn.prepareStatement(modelSql)) {
                    ps.setString(1, brand);
                    ps.setString(2, line);
                    ps.setString(3, color);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            modelId = rs.getInt(1);
                        } else {
                            throw new SQLException("Не удалось получить ID модели.");
                        }
                    }
                }
                System.out.println("   -> Шаг 1: ID модели определен (успешно взят существующий или создан новый): " + modelId);

                // 2. Привязка размера к модели
                try (PreparedStatement ps = conn.prepareStatement(linkSql)) {
                    ps.setInt(1, modelId);
                    ps.setInt(2, sizeId);
                    ps.executeUpdate();
                }
                System.out.println("   -> Шаг 2: Связь модели и размера проверена/создана");

                // 3. Формирование и запуск пакета (Batch) на 15 лотов
                try (PreparedStatement ps = conn.prepareStatement(lotSql)) {
                    for (int row = 1; row <= 3; row++) {
                        BigDecimal basePrice = new BigDecimal("14000.00");
                        BigDecimal multiplier = (row == 3) ? new BigDecimal("1.50") : new BigDecimal("1.00");
                        BigDecimal finalPrice = basePrice.multiply(multiplier);

                        for (int col = 1; col <= 5; col++) {
                            ps.setBigDecimal(1, finalPrice);
                            ps.setInt(2, sellerId);
                            ps.setInt(3, modelId);
                            ps.setInt(4, sizeId);
                            ps.addBatch();
                        }
                    }

                    long startTime = System.currentTimeMillis();
                    ps.executeBatch();
                    long endTime = System.currentTimeMillis();
                    System.out.println("   -> Шаг 3: Пакет из 15 лотов подготовлен в буфере за " + (endTime - startTime) + " мс.");
                }

                // ❓ Интерактивный коммит
                System.out.print("\n❓ Подтвердить транзакцию пакетной вставки? (yes/no): ");
                String choice = scanner.nextLine().trim().toLowerCase();

                if ("yes".equals(choice) || "y".equals(choice)) {
                    conn.commit();
                    System.out.println("   ✅ Пакетная вставка успешно закоммичена. (COMMIT).");
                } else {
                    conn.rollback();
                    System.out.println("   ❌ Изменения отменены (ROLLBACK).");
                }

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("   ❌ Ошибка батча. Полный откат транзакции: " + e.getMessage());
                throw e;
            }
        }
    }
}
