package Task1_BackTracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FactoryOptimizationSolver {

   private final int[] orders;
   private final Factory[] factories;
   private final int deadline;

   // Глобальные переменные для хранения наилучшего найденного решения в ходе рекурсии.
   // Использование Long.MAX_VALUE в качестве начального значения — стандартный прием.
   private long minCost = Long.MAX_VALUE;
   private List<Integer>[] bestAssignment = null;

   public FactoryOptimizationSolver(int deadline, int[] orders, Factory[] factories) {
      this.deadline = deadline;
      this.factories = factories;

      // --- Оптимизация №1: Эффективная эвристика. ---
      // Сортируем заказы от большего к меньшему.
      // Размещение самых "требовательных" (больших) заказов в первую очередь позволяет быстрее обнаружить нежизнеспособные ветви рекурсии (где превышен дедлайн) и отсечь их.
      // Это значительно сужает пространство поиска.
      this.orders = Arrays.stream(orders).boxed().sorted((a, b) -> Integer.compare(b, a)).mapToInt(Integer::intValue).toArray();
   }

   /**
    * Статический метод для заполнения предопределенными данными для демонстрации.
    *
    * @return Объект Task1_BackTracking.ProblemData с данными из примера.
    */
   public static ProblemData fillSampleData() {
      int deadline = 8;
      int[] orders = {5, 5, 1, 1, 1};
      Factory[] factories = { new Factory(1, 1, 1, 7, 3),
                              new Factory(2, 1, 4, 1, 3),
                              new Factory(3, 6, 2, 5, 1)};
      return new ProblemData(deadline, orders, factories);
   }

   /**
    * Статический метод для генерации случайного набора исходных данных.
    *
    * @return Объект Task1_BackTracking.ProblemData, содержащий все необходимые входные параметры.
    */
   public static ProblemData generateSampleData() {
      Random rand = new Random();

      // 1. Сгенерировать случайное количество заказов (от 3 до 8)
      int orderCount = rand.nextInt(6) + 3;
      int[] orders = new int[orderCount];
      for (int i = 0; i < orderCount; i++) {
         orders[i] = (rand.nextInt(100) + 1) * 10; // Заказы от 10 до 1000, кратные 10
      }

      // 2. Сгенерировать случайное количество фабрик (от 2 до 4)
      int factoryCount = rand.nextInt(3) + 2;
      Factory[] factories = new Factory[factoryCount];
      for (int i = 0; i < factoryCount; i++) {
         int id = i + 1;
         int productionPerDay = (rand.nextInt(20) + 1) * 10; // Производительность от 10 до 200
         int costPerShirt = rand.nextInt(10) + 1;            // Цена от 1 до 10
         int setupCost = (rand.nextInt(50) + 5) * 10;        // Стоимость наладки от 50 до 550
         int setupDays = rand.nextInt(5) + 1;                // Дни на наладку от 1 до 5
         factories[i] = new Factory(id, productionPerDay, costPerShirt, setupCost, setupDays);
      }

      // 3. Сгенерировать реалистичный дедлайн
      int totalShirts = Arrays.stream(orders).sum();
      int totalDailyProduction = Arrays.stream(factories).mapToInt(Factory::productionPerDay).sum();
      int roughDays = (totalDailyProduction > 0) ? (totalShirts + totalDailyProduction - 1) / totalDailyProduction : 100;
      int deadline = (int) (roughDays * (0.9 + rand.nextDouble() * 0.6)) + 3; // Дедлайн в диапазоне 90%-150% от грубой оценки

      return new ProblemData(deadline, orders, factories);
   }

   public static void main(String[] args) {
      // --- Получение исходных данных из генератора ---
      // Task1_BackTracking.ProblemData data = generateSampleData();  // Для случайных данных
      ProblemData data = fillSampleData();         // Для демонстрационных данных

      // --- Вывод сгенерированных данных для наглядности ---
      System.out.println("--- Входные данные ---");
      System.out.println("Максимальный срок (дедлайн): " + data.deadline() + " дней");
      System.out.println("Заказы (количество футболок): " + Arrays.toString(data.orders()));
      System.out.println("Фабрики:");
      for (Factory f : data.factories()) {
         System.out.printf("\tID: %d, Производительность: %d/день, Цена: %d/шт, Наладка: %d (стоимость), %d (дни)\n", f.id(), f.productionPerDay(), f.costPerShirt(), f.setupCost(), f.setupDays());
      }
      System.out.println("-------------------------------------\n");

      FactoryOptimizationSolver solver = new FactoryOptimizationSolver(data.deadline(), data.orders(), data.factories());
      AssignmentResult result = solver.solve();

      System.out.println("--- Результат поиска оптимального решения ---");

      if (result.totalCost() == -1) {
         System.out.println("- Невозможно выполнить все заказы в установленный срок (" + data.deadline() + " дней).");
      }
      else {
         System.out.println("+ Минимальная общая стоимость: " + result.totalCost());
         System.out.println("\n--- Детальный план распределения заказов ---");
         for (FactoryAssignment assignment : result.assignments()) {
            String ordersStr = assignment.orders().stream().map(String::valueOf).collect(Collectors.joining(", "));
            System.out.printf("Фабрика #%d: \n\t- Заказы (кол-во футболок): [ %s ]\n\t- Общее время выполнения: %d дней\n\t- Общая стоимость по фабрике: %d\n", assignment.factory().id(), ordersStr, assignment.totalDays(), assignment.totalCost());
         }
      }
   }

   /**
    * Публичный метод для запуска решения.
    *
    * @return Результат с минимальной стоимостью и планом распределения, или -1, если решения нет.
    */
   public AssignmentResult solve() {
      // Инициализация структур для отслеживания текущего состояния в рекурсии.
      @SuppressWarnings("unchecked") List<Integer>[] currentAssignment = new ArrayList[factories.length];
      for (int i = 0; i < factories.length; i++) {
         currentAssignment[i] = new ArrayList<>();
      }
      int[] factoryDays = new int[factories.length];

      // Запуск рекурсивного поиска с первого заказа (индекс 0) и начальной стоимостью 0.
      backtrack(0, currentAssignment, factoryDays, 0);

      // Если стоимость не изменилась, значит, ни одного полного решения не было найдено.
      if (minCost == Long.MAX_VALUE) {
         return new AssignmentResult(-1, null);
      }

      // Формирование детального результата на основе лучшего найденного распределения.
      List<FactoryAssignment> detailedAssignments = new ArrayList<>();
      for (int i = 0; i < factories.length; i++) {
         if (bestAssignment != null && !bestAssignment[i].isEmpty()) {
            long finalFactoryCost = 0;
            int finalFactoryDays = 0;
            for (int orderSize : bestAssignment[i]) {
               finalFactoryDays += factories[i].timeForOrder(orderSize);
               finalFactoryCost += factories[i].costForOrder(orderSize);
            }
            detailedAssignments.add(new FactoryAssignment(factories[i], bestAssignment[i], finalFactoryDays, finalFactoryCost));
         }
      }

      return new AssignmentResult(minCost, detailedAssignments);
   }

   /**
    * Основной рекурсивный метод поиска с возвратом (backtracking).
    *
    * @param orderIndex        Индекс текущего заказа, который нужно распределить.
    * @param currentAssignment Текущее (частичное) распределение заказов по фабрикам.
    * @param factoryDays       Массив с текущей суммарной загрузкой каждой фабрики в днях.
    * @param currentTotalCost  Суммарная стоимость текущего частичного решения.
    */
   private void backtrack(int orderIndex, List<Integer>[] currentAssignment, int[] factoryDays, long currentTotalCost) {
      // --- Оптимизация №2: Отсечение по стоимости ---
      // Если текущая стоимость уже выше или равна найденной минимальной,
      // дальнейший поиск в этой ветви рекурсии бессмысленен, так как он не даст лучшего результата.
      if (currentTotalCost >= minCost) {
         return;
      }

      if (orderIndex == orders.length) {
         minCost = currentTotalCost;
         this.bestAssignment = new ArrayList[factories.length];
         for (int i = 0; i < factories.length; i++) {
            this.bestAssignment[i] = new ArrayList<>(currentAssignment[i]);
         }
         return;
      }

      int currentOrder = orders[orderIndex];
      for (int i = 0; i < factories.length; i++) {
         Factory currentFactory = factories[i];
         int timeForThisOrder = currentFactory.timeForOrder(currentOrder);

         // --- Оптимизация №3: Отсечение по ограничениям ---
         // Проверяем основное условие: не будет ли превышен дедлайн.
         // Если да, то эту фабрику для данного заказа даже не рассматриваем.
         if (factoryDays[i] + timeForThisOrder <= deadline) {
            currentAssignment[i].add(currentOrder);
            factoryDays[i] += timeForThisOrder;
            long costForThisOrder = currentFactory.costForOrder(currentOrder);

            backtrack(orderIndex + 1, currentAssignment, factoryDays, currentTotalCost + costForThisOrder);

            factoryDays[i] -= timeForThisOrder;
            currentAssignment[i].remove(currentAssignment[i].size() - 1);
         }
      }
   }
}
