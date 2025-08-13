package Task2_DynamicProgramming;

import java.util.List;

/**
 * Класс-запись для хранения информации о билете.
 * Использование 'record' упрощает создание неизменяемых классов-носителей данных.
 */
record Ticket(int id, int startDay, int endDay, int matches) {}

/**
 * Класс-запись для хранения итогового результата.
 */
record SchedulingResult(int maxMatches, List<Ticket> chosenTickets) {}

/**
 * Класс-запись для инкапсуляции всего набора исходных данных для задачи.
 */
record ProblemData(List<Ticket> tickets) {}