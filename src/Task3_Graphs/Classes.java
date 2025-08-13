package Task3_Graphs;

import java.util.Map;

/**
 * Класс-запись для представления ребра графа (пути между городами).
 */
record Edge(int to, int distance, int time) {}

/**
 * Класс-запись для представления состояния в алгоритме Дейкстры.
 * Хранит текущий город, общее время и общее расстояние от старта.
 * Реализует Comparable для использования в PriorityQueue.
 */
record PathState(int city, int totalTime, int totalDistance) implements Comparable<PathState> {
   @Override
   public int compareTo(PathState other) {
      // Приоритет отдается пути с МЕНЬШИМ временем.
      if (this.totalTime != other.totalTime) {
         return Integer.compare(this.totalTime, other.totalTime);
      }
      // Если время одинаковое, приоритет отдается пути с БОЛЬШИМ расстоянием.
      return Integer.compare(other.totalDistance, this.totalDistance);
   }
}

/**
 * Класс-запись для хранения результатов работы алгоритма Дейкстры.
 */
record DijkstraResult(Map<Integer, Integer> minTimes, Map<Integer, Integer> maxDistances, Map<Integer, Integer> predecessors) {}
