package Task4_Greedy;

import java.util.*;

/**
 * Основной класс, решающий задачу с помощью жадного алгоритма.
 */
public class StreamingScheduler {

   public static List<Festival> generateSampleData() {
      List<Festival> festivals = new ArrayList<>();
      Random rand = new Random();
      int festivalCount = rand.nextInt(8) + 8; // от 8 до 15 фестивалей
      for (int i = 0; i < festivalCount; i++) {
         int startDay = rand.nextInt(5) + 1;
         int startHour = rand.nextInt(20);
         int durationHours = rand.nextInt(5) + 1; // длительность от 1 до 5 часов

         int absoluteStart = startDay * 24 + startHour;
         int absoluteEnd = absoluteStart + durationHours;

         int endDay = absoluteEnd / 24;
         int endHour = absoluteEnd % 24;

         festivals.add(new Festival(i + 1, startDay, startHour, endDay, endHour));
      }
      return festivals;
   }

   public static void main(String[] args) {
      List<Festival> festivals = fillSampleData();
      // List<Festival> festivals = generateSampleData();
      int costPerDay = 1000; // X = 1000 €

      System.out.println("--- Входные данные: Праздники ---");
      festivals.forEach(f ->
              System.out.printf("ID #%d: День %d, %02d:00 -> День %d, %02d:00\n", f.id(), f.startDay(), f.startHour(), f.endDay(), f.endHour())
      );
      System.out.printf("\nСтоимость канала в день: %d €\n", costPerDay);
      System.out.println("-------------------------------------\n");

      StreamingScheduler scheduler = new StreamingScheduler();
      ScheduleResult result = scheduler.solve(festivals, costPerDay);

      System.out.println("--- Результаты ---");
      System.out.println(" Минимальное необходимое количество каналов: " + result.channelCount());

      System.out.println("\n️ Расписание по каналам:");
      result.schedule().entrySet().stream()
              .sorted(Map.Entry.comparingByKey())
              .forEach(entry -> {
                 System.out.printf("  Канал #%d:\n", entry.getKey());
                 entry.getValue().forEach(f ->
                         System.out.printf("\t- ID #%d (День %d, %02d:00 -> День %d, %02d:00)\n", f.id(), f.startDay(), f.startHour(), f.endDay(), f.endHour())
                 );
              });

      System.out.println("\n Общая стоимость трансляций: " + result.totalCost() + " €");
   }

   // --- Методы для генерации данных ---

   public static List<Festival> fillSampleData() {
      return new ArrayList<>(Arrays.asList(
              new Festival(1, 1, 6, 1, 9),    // Канал 1
              new Festival(2, 1, 10, 1, 12),  // Канал 2
              new Festival(3, 1, 8, 1, 11),   // Канал 3 (пересекается с 1 и 2)
              new Festival(4, 1, 9, 1, 11),   // Канал 1 (начинается, когда 1-й освободился)
              new Festival(5, 2, 13, 2, 15),  // Любой канал
              new Festival(6, 1, 13, 2, 14)   // Канал 2 (начинается, когда 2-й освободился)
      ));
   }

   /**
    * Основной метод, который решает все три части задачи.
    *
    * @param festivals            Список всех фестивалей.
    * @param costPerChannelPerDay Стоимость одного канала за один день использования.
    *
    * @return Объект с результатами: количество каналов, расписание и общая стоимость.
    */
   public ScheduleResult solve(List<Festival> festivals, int costPerChannelPerDay) {
      if (festivals == null || festivals.isEmpty()) {
         return new ScheduleResult(0, new HashMap<>(), 0);
      }

      // 1. Жадный выбор: Сортируем фестивали по времени их НАЧАЛА.
      // Это позволяет нам обрабатывать события в хронологическом порядке.
      festivals.sort(Comparator.comparingInt(Festival::getAbsoluteStartTime));

      // Карта для хранения итогового расписания: Kлюч - номер канала, значение - список фестивалей.
      Map<Integer, List<Festival>> schedule = new HashMap<>();

      // Очередь с приоритетом для хранения свободных каналов.
      // Она будет хранить состояния каналов и всегда возвращать тот, который освободится раньше всех.
      PriorityQueue<ChannelState> availableChannels = new PriorityQueue<>();

      int channelCounter = 0;

      // 2. Итерация и распределение
      for (Festival festival : festivals) {
         // Проверяем, есть ли в очереди канал, который освободится ДО начала текущего фестиваля.
         if (!availableChannels.isEmpty() && availableChannels.peek().freeTime() <= festival.getAbsoluteStartTime()) {
            // Жадное решение: ДА, ЕСТЬ! Используем его.
            // Это лучший локальный выбор, так как мы повторно используем ресурс вместо создания нового.
            ChannelState reusableChannel = availableChannels.poll();

            // Добавляем фестиваль в расписание этого канала.
            schedule.get(reusableChannel.channelIndex()).add(festival);

            // Обновляем время освобождения этого канала и возвращаем его в очередь.
            availableChannels.add(new ChannelState(reusableChannel.channelIndex(), festival.getAbsoluteEndTime()));
         }
         else {
            // Жадное решение: НЕТ свободных каналов. Мы ВЫНУЖДЕНЫ открыть новый.
            channelCounter++;
            int newChannelId = channelCounter;

            // Создаем для него расписание.
            schedule.put(newChannelId, new ArrayList<>(List.of(festival)));

            // Добавляем новый канал в очередь доступных.
            availableChannels.add(new ChannelState(newChannelId, festival.getAbsoluteEndTime()));
         }
      }

      // 3. Расчет стоимости
      long totalCost = calculateTotalCost(schedule, costPerChannelPerDay);

      return new ScheduleResult(channelCounter, schedule, totalCost);
   }

   /**
    * Рассчитывает общую стоимость на основе сгенерированного расписания.
    */
   private long calculateTotalCost(Map<Integer, List<Festival>> schedule, int costPerDay) {
      long totalActiveChannelDays = 0;

      // Проходим по каждому каналу в расписании.
      for (List<Festival> channelFestivals : schedule.values()) {
         // Используем Set, чтобы считать каждый день использования канала только один раз.
         Set<Integer> activeDaysForThisChannel = new HashSet<>();

         // Проходим по каждому фестивалю на данном канале.
         for (Festival festival : channelFestivals) {
            // Добавляем в сет все дни, в которые был активен этот фестиваль.
            for (int day = festival.startDay(); day <= festival.endDay(); day++) {
               activeDaysForThisChannel.add(day);
            }
         }
         totalActiveChannelDays += activeDaysForThisChannel.size();
      }

      return totalActiveChannelDays * costPerDay;
   }
}
