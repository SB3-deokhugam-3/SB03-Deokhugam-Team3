package com.sprint.deokhugam.domain.poweruser.batch.reader;

import com.sprint.deokhugam.domain.poweruser.dto.batch.DateRange;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepositoryImpl;
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
    private boolean exhausted = false;

    // Period 설정 메서드
    public void setPeriod(PeriodType period) {
        this.period = period;
        this.initialized = false; // 새로운 period 설정 시 초기화 상태 리셋
        this.userDataIterator = null; // Iterator도 리셋
        this.exhausted = false;
        log.info("PowerUserDataReader period 설정: {}", period);
    }

    // Factory 메서드 - 기존 인스턴스를 복사해서 새로운 period로 설정
    public static PowerUserDataReader createForPeriod(PowerUserRepository repository, PeriodType period) {
        PowerUserDataReader reader = new PowerUserDataReader(repository);
        reader.setPeriod(period);
        log.info("PowerUserDataReader 팩토리 메서드로 생성: {}", period);
        return reader;
    }

//    @Override
//    public PowerUserData read() throws Exception {
//        log.info("=== PowerUserDataReader.read() 호출 - period: {}, initialized: {} ===", period, initialized);
//
//        // 초기화되지 않았거나 Iterator가 null인 경우 초기화
//        if (!initialized || userDataIterator == null) {
//            log.info("초기화 필요 - period: {}", period);
//            initializeData();
//            initialized = true;
//        } else {
//            log.info("이미 초기화됨 - period: {}, Iterator null 여부: {}", period, userDataIterator == null);
//        }
//
//        PowerUserData result = (userDataIterator != null && userDataIterator.hasNext()) ? userDataIterator.next() : null;
//
//        if (result != null) {
//            log.info("PowerUserDataReader.read() 결과: 사용자={}", result.user().getNickname());
//        } else {
//            log.info("PowerUserDataReader.read() 결과: null (더 이상 데이터 없음)");
//        }
//
//        return result;
//    }

    @Override
    public PowerUserData read() throws Exception {
        log.info("=== PowerUserDataReader.read() 호출 - period: {}, initialized: {}, exhausted: {} ===",
            period, initialized, exhausted);

        // 처음 호출 시에만 초기화
        if (!initialized) {
            log.info("첫 번째 호출 - 초기화 시작");
            try {
                initializeData();
                initialized = true;
            } catch (Exception e) {
                log.error("PowerUserDataReader 초기화 실패", e);
                exhausted = true;
                return null;
            }
        }

        // 이미 모든 데이터를 읽었으면 null 반환
        if (exhausted || userDataIterator == null) {
            log.info("모든 데이터를 읽었거나 Iterator가 null입니다. null 반환");
            return null;
        }

        PowerUserData result = userDataIterator.hasNext() ? userDataIterator.next() : null;

        // 더 이상 데이터가 없으면 exhausted 상태로 변경
        if (result == null || !userDataIterator.hasNext()) {
            exhausted = true;
            log.info("모든 데이터 읽기 완료 - exhausted 상태로 변경");
        }

        if (result != null) {
            log.info("PowerUserDataReader.read() 결과: 사용자={}",
                result.user().getNickname());
        } else {
            log.info("PowerUserDataReader.read() 결과: null (더 이상 데이터 없음)");
        }

        return result;
    }

    private void initializeData() {
        log.info("=== PowerUserDataReader 초기화 시작 - period: {} ===", period);

        if (period == null) {
            log.warn("Period가 설정되지 않았습니다. 빈 데이터를 반환합니다.");
            this.userDataIterator = List.<PowerUserData>of().iterator();
            return;
        }

        if (powerUserRepository == null) {
            log.error("PowerUserRepository가 null입니다!");
            this.userDataIterator = List.<PowerUserData>of().iterator();
            return;
        }

        DateRange dateRange = getDateRange();
        log.info("데이터 조회 시작 - 기간: {}, 범위: {}", period, dateRange.toReadableString());

        List<PowerUserData> userData = List.of();
        try {
            // 먼저 간단한 쿼리부터 시도
            if (powerUserRepository instanceof PowerUserRepositoryImpl impl) {
                log.info("간단한 쿼리로 데이터 조회 시도...");
                userData = impl.findUserActivityDataSimple(period, dateRange.startDate(), dateRange.endDate());
                log.info("간단한 쿼리 결과: {} 건", userData.size());

                if (userData.isEmpty()) {
                    log.info("간단한 쿼리 결과 없음. 복잡한 쿼리 시도...");
                    userData = powerUserRepository.findUserActivityData(period, dateRange.startDate(), dateRange.endDate());
                    log.info("복잡한 쿼리 결과: {} 건", userData.size());
                }
            } else {
                log.info("복잡한 쿼리로 데이터 조회...");
                userData = powerUserRepository.findUserActivityData(period, dateRange.startDate(), dateRange.endDate());
                log.info("복잡한 쿼리 결과: {} 건", userData.size());
            }

            // 데이터가 없는 경우 기본 사용자라도 생성하도록 추가 로직
            if (userData.isEmpty()) {
                log.warn("조회된 활동 데이터가 없습니다. 기본 사용자 활동 데이터 조회를 시도합니다.");
                userData = createFallbackUserData(dateRange);
            }

        } catch (Exception e) {
            log.error("데이터 조회 중 오류 발생 - period: {}", period, e);
            userData = List.of();
        }

        this.userDataIterator = userData.iterator();
        log.info("=== PowerUser Reader 초기화 완료 - {} 기간, {} 건 ===", period, userData.size());

        // 샘플 데이터 로그 출력
        userData.stream()
            .limit(3)
            .forEach(data -> log.info("샘플 데이터: 사용자={}",
                data.user().getNickname()));

        if (userData.isEmpty()) {
            log.warn("조회된 데이터가 없습니다. 데이터베이스에 {} 기간의 활동 데이터가 있는지 확인하세요.", period);
        }
    }

    // 기본 데이터가 없을 때 최소한의 사용자 데이터라도 조회하는 메서드
    private List<PowerUserData> createFallbackUserData(DateRange dateRange) {
        try {
            // 최소한 활동이 있는 사용자들을 조회 (리뷰, 좋아요, 댓글 중 하나라도 있는)
            if (powerUserRepository instanceof PowerUserRepositoryImpl impl) {
                return impl.findUserActivityDataSimple(period, dateRange.startDate(), dateRange.endDate());
            }
        } catch (Exception e) {
            log.error("기본 사용자 데이터 조회 실패", e);
        }
        return List.of();
    }

    private DateRange getDateRange() {
        return switch (period) {
            case DAILY -> DateRange.today();
            case WEEKLY -> DateRange.lastWeek();
            case MONTHLY -> DateRange.lastMonth();
            case ALL_TIME -> DateRange.allTime();
        };
    }
}