package dao;

import db.ConnectionPool;
import java.sql.*;

public class OrderDao {
    public void printAverageCheckReport() throws SQLException {
        String sql =
                "SELECT " +
                        "  CASE " +
                        "    WHEN b.rating < 2.5 THEN 'Новичок (Рейтинг < 2.5)' " +
                        "    WHEN b.rating >= 2.5 AND b.rating < 5.0 THEN 'Любитель (Рейтинг 2.5-5)' " +
                        "    WHEN b.rating >= 5.0 AND b.rating < 7.5 THEN 'Продвинутый (Рейтинг 5-7.5)' " +
                        "    ELSE 'Элита / VIP (Рейтинг 7.5+)' " +
                        "  END AS тип_покупателя, " +
                        "  COUNT(o.id) AS заказов, " +
                        "  ROUND(AVG(o.final_price), 2) AS средний_чек " +
                        "FROM marketplace.orders o " +
                        "JOIN marketplace.buyers b ON o.buyer_id = b.id " +
                        "GROUP BY " +
                        "  CASE " +
                        "    WHEN b.rating < 2.5 THEN 'Новичок (Рейтинг < 2.5)' " +
                        "    WHEN b.rating >= 2.5 AND b.rating < 5.0 THEN 'Любитель (Рейтинг 2.5-5)' " +
                        "    WHEN b.rating >= 5.0 AND b.rating < 7.5 THEN 'Продвинутый (Рейтинг 5-7.5)' " +
                        "    ELSE 'Элита / VIP (Рейтинг 7.5+)' " +
                        "  END";

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("-----------------------------------------------------------------------");
            System.out.printf("| %-32s | %-14s | %-14s |\n", "Категория покупателя", "Кол-во заказов", "Средний чек");
            System.out.println("-----------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("| %-32s | %-14d | %-14s |\n",
                        rs.getString("тип_покупателя"),
                        rs.getInt("заказов"),
                        rs.getBigDecimal("средний_чек").toString()
                );
            }
            System.out.println("-----------------------------------------------------------------------");
        }
    }
}
