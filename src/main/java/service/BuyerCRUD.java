package service;

import dao.BuyerDao;
import db.ConnectionPool;
import model.Buyer;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class BuyerCRUD {
    public static void main(String[] args) {
        System.out.println("\n--- Cервис CRUD Покупателей ---");
        BuyerDao buyerDao = new BuyerDao();

        try {
            // 📥 1. CREATE — Добавляем нового сникерхеда
            System.out.println("\n1. Тест созданиz (CREATE):");
            Buyer newBuyer = new Buyer("Никита Реселлер", new BigDecimal("7.50"));
            int generatedId = buyerDao.insert(newBuyer);
            System.out.println("   ✅ Успех! Добавлен покупатель с ID: " + generatedId);


            // 🔍 2. READ ALL — Читаем всех, кто есть в базе
            System.out.println("\n2. Тест чтения всех (READ ALL):");
            printAll(buyerDao);


            // 🔄 3. UPDATE — Обновляем имя и рейтинг только что созданного чела
            System.out.println("\n3. Тест обновления (UPDATE):");
            Buyer updatedBuyer = new Buyer(generatedId, "Никита Топ-Реселлер", new BigDecimal("9.20"));
            boolean isUpdated = buyerDao.update(updatedBuyer);
            System.out.println("   Список после обновления:");
            printAll(buyerDao);


            // ❌ 4. DELETE — Удаляем временного чела, чтобы не засорять базу
            System.out.println("\n4. Тест удаления (DELETE):");
            boolean isDeleted = buyerDao.delete(generatedId);
            System.out.println("   Финальный список в базе:");
            printAll(buyerDao);

        } catch (SQLException e) {
            System.err.println("❌ Критическая ошибка при CRUD-операциях: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionPool.close(); // Железно закрываем пул соединений
        }
    }

    // Вспомогательный метод для красивого вывода списка в консоль
    private static void printAll(BuyerDao dao) throws SQLException {
        List<Buyer> buyers = dao.findAll();
        if (buyers.isEmpty()) {
            System.out.println("   [Таблица buyers пуста]");
        } else {
            for (Buyer b : buyers) {
                System.out.println("   -> ID: " + b.getId() + " | Имя: " + b.getFullName() + " | Рейтинг: " + b.getRating());
            }
        }
    }
}
