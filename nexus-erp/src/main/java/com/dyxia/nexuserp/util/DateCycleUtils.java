package com.dyxia.nexuserp.util;

import java.time.LocalDate;

/**
 * Utilitaire pour le calcul des cycles mensuels de paie (du 16 au 15).
 */
public class DateCycleUtils {

    /**
     * Calcule la plage de dates pour le cycle mensuel contenant la date fournie.
     * Le cycle mensuel s'étend du 16 du mois N au 15 du mois N+1.
     *
     * @param date La date de référence.
     * @return Tableau de deux LocalDate [startDate, endDate] du cycle.
     */
    public static LocalDate[] getMonthlyCycleRange(LocalDate date) {
        if (date.getDayOfMonth() >= 16) {
            LocalDate startDate = LocalDate.of(date.getYear(), date.getMonth(), 16);
            LocalDate endDate = startDate.plusMonths(1).withDayOfMonth(15);
            return new LocalDate[]{startDate, endDate};
        } else {
            LocalDate endDate = LocalDate.of(date.getYear(), date.getMonth(), 15);
            LocalDate startDate = endDate.minusMonths(1).withDayOfMonth(16);
            return new LocalDate[]{startDate, endDate};
        }
    }
}
