package service;

import dao.LotDao;
import db.ConnectionPool;
import java.sql.SQLException;
import java.util.Scanner;

public class LotBatch {
    public static void main(String[] args) {
        System.out.println("\n--- Пакетная Вставка ---");
        Scanner scanner = new Scanner(System.in);

        LotDao lotDao = new LotDao();

        // Тестовые входные параметры
        int sellerId = 1;
        int sizeId = 2;
        String brand = "Nike";
        String line = "Air Max 90";
        String color = "Infrared";

        try {
            lotDao.executeBatchLotInsertion(brand, line, color, sellerId, sizeId, scanner);
        } catch (SQLException e) {
            System.err.println("Критическая ошибка выполнения сервиса: " + e.getMessage());
        } finally {
            ConnectionPool.close();
        }
    }
}
