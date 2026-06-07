package service;

import dao.OrderDao;
import db.ConnectionPool;
import java.sql.SQLException;

public class OrderReport {
    public static void main(String[] args) {
        System.out.println("\n--- Бизнес-отчет: Средний чек ---");

        OrderDao orderDao = new OrderDao();
        try {
            orderDao.printAverageCheckReport();
        } catch (SQLException e) {
            System.err.println("Ошибка построения отчета: " + e.getMessage());
        } finally {
            ConnectionPool.close();
        }
    }
}