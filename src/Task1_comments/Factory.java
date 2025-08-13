package Task1_comments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Factory {
   private int perDay;      // Производительность (футболок/день)
   private int price;       // Стоимость производства одной футболки
   private int setUpDays;   // Дни на наладку производства
   private int setUpCosts;  // Стоимость наладки производства

   public Factory(int perDay, int price, int setUpDays, int setUpCosts) {
      this.perDay = perDay;
      this.price = price;
      this.setUpDays = setUpDays;
      this.setUpCosts = setUpCosts;
   }

   public static void main(String[] args) {
      int deadline = 8;
      int[] orders = {5, 5, 1, 1, 1};
      Factory[] factories = {
              new Factory(1, 1, 3, 7), // Фабрика 1
              new Factory(1, 4, 3, 1), // Фабрика 2
              new Factory(6, 2, 1, 5)  // Фабрика 3
      };

      Solver solver = new Solver(orders, deadline, factories);

      int result = solver.solve();
      System.out.println("Результат: " + result);
      if (result != -1) {
         solver.printSolution();
      }
   }

   // Геттеры (если понадобятся в будущем)
   public int getPerDay() {
      return perDay;
   }

   public int getPrice() {
      return price;
   }

   public int getSetUpDays() {
      return setUpDays;
   }

   public int getSetUpCosts() {
      return setUpCosts;
   }

   // Новый метод: расчёт времени выполнения заказа
   public int getTimeForOrder(int orderSize) {
      int productionTime = (orderSize + perDay - 1) / perDay;
      return setUpDays + productionTime;
   }

   // Новый метод: расчёт стоимости выполнения заказа
   public int getCostForOrder(int orderSize) {
      return orderSize * price + setUpCosts;
   }

   public static class Solver {
      private int[] orders;  //Массив заказов
      private int deadline;  //Срок выполнения
      private int minCost = Integer.MAX_VALUE;
      private Factory[] factories;
      private int[] best; //лучшее распределение
      private int[] currentAssignment; //текущее распределение
      private int[] currentFactoryDays; //текущая загрузка

      public Solver(int[] orders, int deadline, Factory[] factories) {
         this.orders = orders;
         this.deadline = deadline;
         this.factories = factories;
         this.best = new int[orders.length];
         this.currentFactoryDays = new int[factories.length];
         this.currentAssignment = new int[orders.length];
         //заполнение массива начальными значениями
         Arrays.fill(currentAssignment, -1);
      }

      private void backtrack(int orderIndex, int currentTotalCost) {
         if (currentTotalCost >= minCost) {
            return;
         }

         if (orderIndex == orders.length) {
            this.minCost = currentTotalCost;
            this.best = currentAssignment.clone();
            return;
         }

         int currOrder = orders[orderIndex];
         // Перебор всех фабрик для текущего заказа
         for (int i = 0; i < factories.length; i++) {
            Factory f = factories[i];
            int timeForOrder = f.getTimeForOrder(currOrder);
            int costForOrder = f.getCostForOrder(currOrder);

            // Проверка ограничения по времени
            if (currentFactoryDays[i] + timeForOrder <= deadline) {
               int prevAssignment = currentAssignment[orderIndex];

               currentAssignment[orderIndex] = i;
               currentFactoryDays[i] += timeForOrder;

               backtrack(orderIndex + 1, currentTotalCost + costForOrder);

               currentFactoryDays[i] -= timeForOrder;
               currentAssignment[orderIndex] = prevAssignment;
            }
         }
      }

      public int solve() {
         backtrack(0, 0);
         return (minCost == Integer.MAX_VALUE) ? -1 : minCost;
      }

      public void printSolution() {
         if (minCost == Integer.MAX_VALUE) {
            System.out.println("Решение не найдено");
            return;
         }

         System.out.println("Минимальная стоимость: " + minCost);
         List<List<Integer>> factoryOrders = new ArrayList<>();
         for (int i = 0; i < factories.length; i++) {
            factoryOrders.add(new ArrayList<>());
         }

         for (int i = 0; i < orders.length; i++) {
            factoryOrders.get(best[i]).add(orders[i]);
         }

         // Вывод информации по каждой фабрике
         for (int i = 0; i < factories.length; i++) {
            Factory f = factories[i];
            List<Integer> ordersList = factoryOrders.get(i);
            System.out.println("\nФабрика " + (i + 1) + ":");

            if (ordersList.isEmpty()) {
               System.out.println("Заказов нет");
               continue;
            }

            int totalTime = 0;
            int totalCost = 0;
            // Расчет времени и стоимости для каждого заказа
            for (int order : ordersList) {
               int time = f.getTimeForOrder(order);
               int cost = f.getCostForOrder(order);
               totalTime += time;
               totalCost += cost;
               System.out.println(" Заказ " + order + " футболок: " +
                       time + " дней, " + " стоимость: " + cost);
            }
            System.out.println("Итого: " + totalTime + " дней, " + totalCost);
         }
      }
   }
}