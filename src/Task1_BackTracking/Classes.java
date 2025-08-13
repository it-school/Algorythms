package Task1_BackTracking;

import java.util.List;

/**
 * Класс-запись для хранения информации о фабрике.
 * Использование 'record' — это современный способ в Java (с 14 версии) для создания
 * неизменяемых классов-носителей данных, что делает код более кратким и безопасным.
 */
record Factory(int id, int productionPerDay, int costPerShirt, int setupCost, int setupDays) {
   /**
    * Рассчитывает время в днях, необходимое для выполнения одного заказа на этой фабрике.
    *
    * @param orderSize Количество футболок в заказе.
    *
    * @return Общее количество дней, включая наладку.
    */
   public int timeForOrder(int orderSize) {
      // Формула (A + B - 1) / B для целых положительных чисел A и B
      // является эквивалентом Math.ceil(A / B) без использования чисел с плавающей запятой.
      int productionDays = (orderSize + this.productionPerDay - 1) / this.productionPerDay;
      return productionDays + this.setupDays;
   }

   /**
    * Рассчитывает стоимость выполнения одного заказа на этой фабрике.
    *
    * @param orderSize Количество футболок в заказе.
    *
    * @return Общая стоимость, включая наладку.
    */
   public int costForOrder(int orderSize) {
      return orderSize * this.costPerShirt + this.setupCost;
   }
}

/**
 * Класс-запись для хранения итогового результата работы алгоритма.
 */
record AssignmentResult(long totalCost, List<FactoryAssignment> assignments) {}

/**
 * Класс-запись для хранения информации о назначенных на одну фабрику заказах.
 * Используется для формирования детального ответа.
 */
record FactoryAssignment(Factory factory, List<Integer> orders, int totalDays, long totalCost) {}

/**
 * Класс-запись для инкапсуляции всего набора исходных данных для задачи.
 */
record ProblemData(int deadline, int[] orders, Factory[] factories) {}