package service;

import dao.BuyerDao;
import model.Buyer;
import db.ConnectionPool;
import java.sql.SQLException;
import java.util.Optional;


public class BuyerSearch {
    public static void main(String[] args) {
        System.out.println("\n--- Параметризированный Запрос ---");

        BuyerDao buyerDao = new BuyerDao();
        String searchName = "Дмитрий Уткин";

        try {
            Optional<Buyer> result = buyerDao.findByFullName(searchName);
            if (result.isPresent()) {
                Buyer b = result.get();
                System.out.println("\n   ✅ Покупатель найден! ID: " + b.getId() + ", Имя: " + searchName + ", Рейтинг: " + b.getRating() + "\n");
            } else {
                System.out.println("\n   ❌ Покупатель '" + searchName + "' не найден в БД.\n");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка выполнения: " + e.getMessage());
        } finally {
            ConnectionPool.close(); // Закрываем пул при выходе
        }
    }
}
