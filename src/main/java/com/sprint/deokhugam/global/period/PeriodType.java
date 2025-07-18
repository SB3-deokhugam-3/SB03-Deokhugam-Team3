package com.sprint.deokhugam.global.period;

import java.time.LocalDate;
import org.springframework.cglib.core.Local;

public enum PeriodType {
    DAILY {
        public LocalDate getStartDate(LocalDate baseDate) {
            return baseDate;
        }
    },
    WEEKLY {
        public LocalDate getStartDate(LocalDate baseDate) {
            return baseDate.minusDays(6);
        }
    },
    MONTHLY {
        public LocalDate getStartDate(LocalDate baseDate) {
            return baseDate.minusMonths(1);
        }
    },
    ALL_TIME {
        public LocalDate getStartDate(LocalDate baseDate) {
            return LocalDate.of(2000, 1, 1);
        }
    };

    public abstract LocalDate getStartDate(LocalDate baseDate);
}
