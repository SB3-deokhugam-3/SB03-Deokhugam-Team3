package com.sprint.deokhugam.domain.poweruser.batch.reader;

import com.sprint.deokhugam.domain.poweruser.dto.batch.DateRange;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerUserDataReader implements ItemReader<PowerUserData> {

    @Getter
    private final PowerUserRepository powerUserRepository;
    private PeriodType period;
    private Iterator<PowerUserData> userDataIterator;
    private boolean initialized = false;

    // Factory 메서드
    public static PowerUserDataReader createForPeriod(PowerUserRepository repository,
        PeriodType period) {
        PowerUserDataReader reader = new PowerUserDataReader(repository);
        reader.setPeriod(period);
        return reader;
    }

    // Period 설정 메서드
    public void setPeriod(PeriodType period) {
        this.period = period;
        this.initialized = false; // 새로운 period 설정 시 초기화 상태 리셋
    }

    @Override
    public PowerUserData read() throws Exception {
        if (!initialized) {
            initializeData();
            initialized = true;
        }

        return userDataIterator.hasNext() ? userDataIterator.next() : null;
    }

    private void initializeData() {
        if (period == null) {
            log.warn("Period가 설정되지 않았습니다. 빈 데이터를 반환합니다.");
            this.userDataIterator = List.<PowerUserData>of().iterator();
            return;
        }

        DateRange dateRange = getDateRange();
        log.info("데이터 조회 시작 - 기간: {}, 범위: {}", period, dateRange);

        // 실제 사용자별 활동 데이터를 조회하여 PowerUserData 생성
        List<PowerUserData> userData = powerUserRepository
            .findUserActivityData(period, dateRange.startDate(), dateRange.endDate());

        this.userDataIterator = userData.iterator();
        log.info("PowerUser Reader 초기화 완료 - {} 기간, {} 건", period, userData.size());

        userData.stream()
            .limit(3)
            .forEach(data -> log.debug("샘플 데이터: {}", data.getUserSummary()));
    }

    private DateRange getDateRange() {
        return switch (period) {
            case DAILY -> DateRange.yesterday();
            case WEEKLY -> DateRange.lastWeek();
            case MONTHLY -> DateRange.lastMonth();
            case ALL_TIME -> DateRange.allTime();
        };
    }
}