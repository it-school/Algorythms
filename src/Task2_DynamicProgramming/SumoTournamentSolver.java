package Task2_DynamicProgramming;

import java.util.*;


public class SumoTournamentSolver {

   /**
    * Статический метод для генерации случайного набора исходных данных.
    * Длительность билета (duration) генерируется отдельно от времени начала,
    * чтобы интервалы были осмысленными (endDay > startDay).
    */
   public static ProblemData generateSampleData() {
      Random rand = new Random();
      List<Ticket> tickets = new ArrayList<>();
      int ticketCount = rand.nextInt(5) + 5; // от 5 до 9 билетов

      for (int i = 0; i < ticketCount; i++) {
         int startDay = rand.nextInt(20) + 1;
         int duration = rand.nextInt(4) + 1; // длительность от 1 до 4 дней
         int endDay = startDay + duration;
         int matches = (rand.nextInt(15) + 1) * 10; // от 10 до 150 матчей
         tickets.add(new Ticket(i + 1, startDay, endDay, matches));
      }
      return new ProblemData(tickets);
   }

   public static void main(String[] args) {
      // ProblemData data = generateSampleData(); // Для случайных данных
      ProblemData data = fillSampleData(); // Для демонстрационных данных

      System.out.println("--- Входные данные: Билеты на турнир ---");
      for (Ticket t : data.tickets()) {
         System.out.printf("Билет #%d: Дни [%d, %d], Матчей: %d\n", t.id(), t.startDay(), t.endDay(), t.matches());
      }
      System.out.println("----------------------------------------\n");

      SumoTournamentSolver solver = new SumoTournamentSolver();
      SchedulingResult result = solver.findMaxMatches(data.tickets());

      System.out.println("--- Результат ---");
      System.out.println("Максимальное количество матчей: " + result.maxMatches());
      System.out.println("\nДля этого необходимо купить следующие билеты:");
      for (Ticket ticket : result.chosenTickets()) {
         System.out.printf("  -> Билет #%d (Дни: %d-%d, Матчей: %d)\n", ticket.id(), ticket.startDay(), ticket.endDay(), ticket.matches());
      }
   }

   /**
    * Статический метод для заполнения предопределенными данными для демонстрации.
    */
   public static ProblemData fillSampleData() {
      List<Ticket> tickets = new ArrayList<>(Arrays.asList(new Ticket(1, 1, 3, 5), new Ticket(2, 2, 5, 6), new Ticket(3, 4, 6, 5), new Ticket(4, 6, 7, 4), new Ticket(5, 5, 8, 11), new Ticket(6, 7, 9, 2)));
      return new ProblemData(tickets);
   }

   /**
    * Основной метод для решения задачи.
    *
    * @param tickets Список всех доступных билетов.
    *
    * @return Результат, содержащий максимальное количество матчей и список выбранных билетов.
    */
   public SchedulingResult findMaxMatches(List<Ticket> tickets) {
      if (tickets == null || tickets.isEmpty()) {
         return new SchedulingResult(0, new ArrayList<>());
      }

      // 1. Сортировка: ключевой шаг. Сортируем билеты по времени их ОКОНЧАНИЯ.
      // Это позволяет нам при рассмотрении i-го билета принимать решение,
      // зная, что все предыдущие билеты (j < i) заканчиваются не позже, чем i-й.
      tickets.sort(Comparator.comparingInt(Ticket::endDay));

      int n = tickets.size();

      // 2. Массив maxMatchesUpTo[i] ("таблица памяти") будет хранить максимальное количество матчей,
      // которое можно посмотреть, используя подмножество из первых i+1 билетов (от 0 до i)
      // из отсортированного списка. 9.	Зная это, мы понимаем, что итоговый ответ на всю задачу
      // будет храниться в последней ячейке — maxMatchesUpTo[n-1]
      int[] maxMatchesUpTo = new int[n];

      // Базовый случай: для первого билета (индекс 0) максимальное количество матчей - это его собственное.
      maxMatchesUpTo[0] = tickets.get(0).matches();

      // 3. Заполнение DP-массива (основной цикл)
      // Итерируемся по отсортированным билетам и для каждого i-го билета заполняем ячейку maxMatchesUpTo[i].
      for (int i = 1; i < n; i++) {
         Ticket currentTicket = tickets.get(i);
         int matchesIfWeTakeCurrent = currentTicket.matches();

         // Находим индекс последнего билета, который НЕ ПЕРЕСЕКАЕТСЯ с текущим.
         // Непересекающийся билет - тот, который заканчивается до начала текущего.
         int prevNonOverlappingIndex = findPreviousNonOverlapping(tickets, i);

         // Если такой билет найден, то к матчам текущего билета можно добавить
         // оптимальное решение, найденное для всех билетов до непересекающегося.
         if (prevNonOverlappingIndex != -1) {
            matchesIfWeTakeCurrent += maxMatchesUpTo[prevNonOverlappingIndex];
         }

         // Основная формула для динамического программирования - оптимальное решение для i билетов
         // - это максимум из двух вариантов:

         // 1. Взять текущий билет (matchesIfWeTakeCurrent).
         // Если мы берём i-й билет, мы получаем tickets.get(i).matches() матчей.
         // Но поскольку мы не можем быть в двух местах одновременно, мы можем добавить
         // к этому результату только матчи с тех билетов, которые не пересекаются с i-м.
         // Благодаря сортировке, нужно лишь найти оптимальное решение для всех билетов,
         // которые заканчиваются до начала i-го билета.

         // 2. НЕ брать текущий билет (в этом случае результат равен maxMatchesUpTo[i-1]).
         // Если мы решаем проигнорировать i-й билет, то максимальное количество матчей,
         // которое мы можем посмотреть, очевидно, не меняется по сравнению с предыдущим шагом.
         // Оно равно оптимальному решению для первых i-1 билетов.
         maxMatchesUpTo[i] = Math.max(matchesIfWeTakeCurrent, maxMatchesUpTo[i - 1]);
      }

      // 4. Восстановление пути (какие билеты были выбраны для достижения оптимума)
      // После заполнения массива maxMatchesUpTo мы знаем ответ (maxMatchesUpTo[n-1]),
      // но не знаем, какие именно билеты его сформировали.
      // Для этого мы "идём по своим следам" в обратном порядке, с конца массива maxMatchesUpTo.
      List<Ticket> chosenTickets = new ArrayList<>();
      int i = n - 1;
      while (i >= 0) {
         // Если мы на первом билете, и его стоит взять (т.е. он вносит вклад в результат)
         if (i == 0 && maxMatchesUpTo[i] > 0) {
            chosenTickets.add(tickets.get(i));
            break;
         }

         // Если maxMatchesUpTo[i] > maxMatchesUpTo[i-1], это означает, что i-й билет ТОЧНО был выбран,
         // так как он улучшил результат по сравнению с предыдущим шагом. Без него оптимум не достигался.
         // Т.е. i-й билет был абсолютно необходим для достижения этого оптимума.
         // Мы добавляем i-й билет в наш итоговый список. После этого мы должны найти остальные билеты,
         // которые были выбраны вместе с ним. Мы знаем, что они не должны с ним пересекаться,
         // поэтому мы "перепрыгиваем" наш указатель i на prevIndex — последнего непересекающегося билета
         // и продолжаем анализ оттуда.
         if (maxMatchesUpTo[i] > maxMatchesUpTo[i - 1]) {
            chosenTickets.add(tickets.get(i));

            // Теперь нам нужно найти остальные билеты, которые привели к этому решению.
            // Мы "перескакиваем" на последний непересекающийся с ним билет, чтобы продолжить поиск оттуда.
            int prevIndex = findPreviousNonOverlapping(tickets, i);
            i = prevIndex;
         }
         else {
            // Если maxMatchesUpTo[i] == maxMatchesUpTo[i-1], значит, i-й билет не был выбран
            // (т.е. он не улучшил результат), поэтому оптимальное решение для i-го шага
            // было просто унаследовано от (i-1)-го.
            // Просто переходим к предыдущему билету.
            i--;
         }
      }

      // Так как мы добавляли билеты с конца (от последнего к первому), развернем список для правильного порядка.
      java.util.Collections.reverse(chosenTickets);

      return new SchedulingResult(maxMatchesUpTo[n - 1], chosenTickets);
   }

   /**
    * Вспомогательный метод для поиска последнего билета, который не пересекается с билетом `i`.
    *
    * @param tickets Отсортированный список билетов.
    * @param i       Индекс текущего билета.
    *
    * @return Индекс непересекающегося билета или -1, если такого нет.
    */
   private int findPreviousNonOverlapping(List<Ticket> tickets, int i) {
      // Простой линейный поиск в обратном порядке. Для небольшого N он достаточно эффективен.
      // Для очень больших N здесь можно было бы использовать бинарный поиск для ускорения.
      for (int j = i - 1; j >= 0; j--) {
         if (tickets.get(j).endDay() < tickets.get(i).startDay()) {
            return j;
         }
      }
      return -1;
   }
}
