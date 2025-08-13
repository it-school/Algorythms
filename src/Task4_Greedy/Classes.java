package Task4_Greedy;

import java.util.List;
import java.util.Map;

/**
 * Класс-запись для представления фестиваля.
 * Включает в себя исходные данные и вычисляемые "абсолютные" временные метки для удобства сравнения и сортировки.
 */
record Festival(int id, int startDay, int startHour, int endDay, int endHour) {
   // Абсолютное время начала в часах от начала времен.
   public int getAbsoluteStartTime() {
      return startDay * 24 + startHour;
   }

   // Абсолютное время окончания в часах.
   public int getAbsoluteEndTime() {
      return endDay * 24 + endHour;
   }
}

/**
 * Вспомогательный класс для хранения состояния канала в PriorityQueue.
 * Хранит индекс канала и время, когда он освободится.
 * Реализует Comparable, чтобы очередь сортировала каналы по времени их освобождения.
 */
record ChannelState(int channelIndex, int freeTime) implements Comparable<ChannelState> {
   @Override
   public int compareTo(ChannelState other) {
      return Integer.compare(this.freeTime, other.freeTime);
   }
}

/**
 * Класс-запись для хранения итоговых результатов.
 */
record ScheduleResult(int channelCount, Map<Integer, List<Festival>> schedule, long totalCost) {}