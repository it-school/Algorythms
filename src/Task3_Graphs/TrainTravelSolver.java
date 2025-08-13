package Task3_Graphs;

import java.util.*;


/**
 * Основной класс, решающий задачу.
 */
public class TrainTravelSolver {

   private final Map<Integer, List<Edge>> adjacencyList;
   private final Map<Integer, String> cityNames;

   public TrainTravelSolver(Map<Integer, String> cityNames) {
      this.adjacencyList = new HashMap<>();
      this.cityNames = cityNames;
      cityNames.keySet().forEach(cityId -> adjacencyList.put(cityId, new ArrayList<>()));
   }

   public static void main(String[] args) {
      Map<Integer, String> cityNames = new HashMap<>();
      cityNames.put(0, "Токио");
      cityNames.put(1, "Киото");
      cityNames.put(2, "Осака");
      cityNames.put(3, "Нагоя");
      cityNames.put(4, "Саппоро");

      TrainTravelSolver solver = new TrainTravelSolver(cityNames);

      solver.addConnection(0, 1, 500, 140); // Токио -> Киото
      solver.addConnection(1, 0, 500, 150); // Киото -> Токио

      solver.addConnection(0, 3, 350, 100); // Токио -> Нагоя
      solver.addConnection(3, 0, 350, 95);  // Нагоя -> Токио

      solver.addConnection(1, 2, 45, 30);   // Киото -> Осака
      solver.addConnection(2, 1, 45, 35);   // Осака -> Киото

      solver.addConnection(3, 1, 150, 50);  // Нагоя -> Киото
      solver.addConnection(1, 3, 150, 55);  // Киото -> Нагоя

      solver.addConnection(0, 4, 1100, 250); // Токио -> Саппоро (гипотетический туннель)
      solver.addConnection(4, 0, 1100, 260); // Саппоро -> Токио

      int startTime = 240; // 4 часа
      int startCity = 0; // Токио

      System.out.printf("--- Условия задачи ---\nНачальный город: %s\nДоступное время: %d минут\n\n", cityNames.get(startCity), startTime);

      solver.solveOneWay(startCity, startTime);
      solver.solveRoundTrip(startCity, startTime);
   }

   /**
    * Добавляет направленное ребро в граф.
    */
   public void addConnection(int from, int to, int distance, int time) {
      adjacencyList.get(from).add(new Edge(to, distance, time));
   }

   /**
    * Решает основное задание: найти самый дальний город в одну сторону.
    */
   public void solveOneWay(int startCityId, int maxTime) {
      DijkstraResult result = runDijkstra(startCityId);

      int farthestCity = -1;
      int maxDist = -1;

      for (int cityId : cityNames.keySet()) {
         if (result.minTimes().get(cityId) <= maxTime) {
            if (result.maxDistances().get(cityId) > maxDist) {
               maxDist = result.maxDistances().get(cityId);
               farthestCity = cityId;
            }
         }
      }

      System.out.println("--- Основное задание (путь в одну сторону) ---");
      if (farthestCity != -1) {
         System.out.printf("Самый дальний город, достижимый за %d минут: %s\n", maxTime, cityNames.get(farthestCity));
         System.out.printf("   - Расстояние: %d км\n", maxDist);
         System.out.printf("   - Время в пути: %d минут\n", result.minTimes().get(farthestCity));
      }
      else {
         System.out.println("Невозможно достичь ни одного города за указанное время.");
      }
   }

   /**
    * Решает дополнительное задание: найти самый дальний город с возможностью вернуться.
    */
   public void solveRoundTrip(int startCityId, int maxTime) {
      // 1. Запускаем Дейкстру из старта, чтобы найти пути "туда".
      DijkstraResult forwardResult = runDijkstra(startCityId);

      // 2. Создаем "обратный" граф, чтобы найти пути "обратно".
      TrainTravelSolver reversedSolver = createReversedSolver();
      DijkstraResult backwardResult = reversedSolver.runDijkstra(startCityId);

      int farthestCity = -1;
      int maxDist = -1;

      for (int cityId : cityNames.keySet()) {
         int timeTo = forwardResult.minTimes().get(cityId);
         int timeFrom = backwardResult.minTimes().get(cityId);

         if (timeTo == Integer.MAX_VALUE || timeFrom == Integer.MAX_VALUE) {
            continue;
         }

         if (timeTo + timeFrom <= maxTime) {
            if (forwardResult.maxDistances().get(cityId) > maxDist) {
               maxDist = forwardResult.maxDistances().get(cityId);
               farthestCity = cityId;
            }
         }
      }

      System.out.println("\n--- Дополнительное задание (путь туда и обратно) ---");
      if (farthestCity != -1) {
         System.out.printf("Самый дальний город с возможностью вернуться за %d минут: %s\n", maxTime, cityNames.get(farthestCity));
         System.out.printf("   - Расстояние: %d км\n", maxDist);
         System.out.printf("   - Время 'туда': %d мин, Время 'обратно': %d мин. Общее: %d мин.\n",
                 forwardResult.minTimes().get(farthestCity),
                 backwardResult.minTimes().get(farthestCity),
                 forwardResult.minTimes().get(farthestCity) + backwardResult.minTimes().get(farthestCity));
      }
      else {
         System.out.println("Невозможно совершить путешествие туда и обратно ни в один город за указанное время.");
      }
   }

   /**
    * Реализация алгоритма Дейкстры для поиска путей с минимальным временем и максимальным расстоянием.
    *
    * @param startCityId ID начального города.
    *
    * @return Результаты работы алгоритма.
    */
   private DijkstraResult runDijkstra(int startCityId) {
      Map<Integer, Integer> minTimes = new HashMap<>();
      Map<Integer, Integer> maxDistances = new HashMap<>();
      Map<Integer, Integer> predecessors = new HashMap<>();

      // PriorityQueue будет автоматически сортировать состояния по времени (и затем по расстоянию).
      PriorityQueue<PathState> pq = new PriorityQueue<>();

      // Инициализация: все времена - бесконечность, все расстояния - 0.
      for (int cityId : cityNames.keySet()) {
         minTimes.put(cityId, Integer.MAX_VALUE);
         maxDistances.put(cityId, 0);
      }

      // Начальное состояние.
      minTimes.put(startCityId, 0);
      pq.add(new PathState(startCityId, 0, 0));

      while (!pq.isEmpty()) {
         PathState currentState = pq.poll();
         int currentCity = currentState.city();
         int currentTime = currentState.totalTime();
         int currentDistance = currentState.totalDistance();

         // Оптимизация: если мы уже нашли более короткий (или такой же по времени, но более длинный по расстоянию)
         // путь до этого города, то текущее состояние можно проигнорировать.
         if (currentTime > minTimes.get(currentCity) ||
                 (currentTime == minTimes.get(currentCity) && currentDistance < maxDistances.get(currentCity))) {
            continue;
         }

         // Исследуем всех соседей текущего города.
         for (Edge edge : adjacencyList.getOrDefault(currentCity, Collections.emptyList())) {
            int neighborCity = edge.to();
            int newTime = currentTime + edge.time();
            int newDistance = currentDistance + edge.distance();

            // Если найден путь с меньшим временем...
            if (newTime < minTimes.get(neighborCity)) {
               minTimes.put(neighborCity, newTime);
               maxDistances.put(neighborCity, newDistance);
               predecessors.put(neighborCity, currentCity);
               pq.add(new PathState(neighborCity, newTime, newDistance));
            }
            // ...или если найден путь с таким же временем, но БОЛЬШИМ расстоянием.
            else {
               if (newTime == minTimes.get(neighborCity) && newDistance > maxDistances.get(neighborCity)) {
                  maxDistances.put(neighborCity, newDistance);
                  predecessors.put(neighborCity, currentCity);
                  pq.add(new PathState(neighborCity, newTime, newDistance));
               }
            }
         }
      }
      return new DijkstraResult(minTimes, maxDistances, predecessors);
   }

   private TrainTravelSolver createReversedSolver() {
      TrainTravelSolver reversed = new TrainTravelSolver(this.cityNames);
      for (Map.Entry<Integer, List<Edge>> entry : this.adjacencyList.entrySet()) {
         int from = entry.getKey();
         for (Edge edge : entry.getValue()) {
            int to = edge.to();
            // Добавляем ребро в обратном направлении
            reversed.addConnection(to, from, edge.distance(), edge.time());
         }
      }
      return reversed;
   }
}
