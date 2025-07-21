package com.sprint.deokhugam.domain.poweruser.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserBatchService 테스트")
public class PowerUserBatchServiceTest {

    @Mock
    private PowerUserRepository powerUserRepository;

    @Mock
    private PowerUserService powerUserService;

    @InjectMocks
    private PowerUserBatchService powerUserBatchService;

    @Test
    void calculateDailyPowerUsers_정상_계산() {
        // given
        List<PowerUser> mockPowerUsers = Arrays.asList(mock(PowerUser.class));
        when(powerUserRepository.calculateAndCreatePowerUsers(eq(PeriodType.DAILY), any(), any()))
            .thenReturn(mockPowerUsers);

        // when
        powerUserBatchService.calculateDailyPowerUsers();

        // then
        verify(powerUserRepository).calculateAndCreatePowerUsers(eq(PeriodType.DAILY), any(), any());
        verify(powerUserService).replacePowerUsers(mockPowerUsers); // replacePowerUsers 검증
        // deleteByPeriod는 replacePowerUsers 내부에서 호출되므로 직접 검증하지 않음
    }

    @Test
    void calculateWeeklyPowerUsers_정상_계산() {
        // given
        List<PowerUser> mockPowerUsers = Arrays.asList(mock(PowerUser.class));
        when(powerUserRepository.calculateAndCreatePowerUsers(eq(PeriodType.WEEKLY), any(), any()))
            .thenReturn(mockPowerUsers);

        // when
        powerUserBatchService.calculateWeeklyPowerUsers();

        // then
        verify(powerUserRepository).calculateAndCreatePowerUsers(eq(PeriodType.WEEKLY), any(), any());
        verify(powerUserService).replacePowerUsers(mockPowerUsers);
    }

    @Test
    void calculateMonthlyPowerUsers_정상_계산() {
        // given
        List<PowerUser> mockPowerUsers = Arrays.asList(mock(PowerUser.class));
        when(powerUserRepository.calculateAndCreatePowerUsers(eq(PeriodType.MONTHLY), any(), any()))
            .thenReturn(mockPowerUsers);

        // when
        powerUserBatchService.calculateMonthlyPowerUsers();

        // then
        verify(powerUserRepository).calculateAndCreatePowerUsers(eq(PeriodType.MONTHLY), any(), any());
        verify(powerUserService).replacePowerUsers(mockPowerUsers);
    }

    @Test
    void calculateAllTimePowerUsers_정상_계산() {
        // given
        List<PowerUser> mockPowerUsers = Arrays.asList(mock(PowerUser.class));
        when(powerUserRepository.calculateAndCreatePowerUsers(PeriodType.ALL_TIME, null, null))
            .thenReturn(mockPowerUsers);

        // when
        powerUserBatchService.calculateAllTimePowerUsers();

        // then
        verify(powerUserRepository).calculateAndCreatePowerUsers(PeriodType.ALL_TIME, null, null);
        verify(powerUserService).replacePowerUsers(mockPowerUsers);
    }

    @Test
    void calculateDailyPowerUsers_빈_결과_처리() {
        // given
        List<PowerUser> emptyList = Collections.emptyList();
        when(powerUserRepository.calculateAndCreatePowerUsers(eq(PeriodType.DAILY), any(), any()))
            .thenReturn(emptyList);

        // when
        powerUserBatchService.calculateDailyPowerUsers();

        // then
        verify(powerUserRepository).calculateAndCreatePowerUsers(eq(PeriodType.DAILY), any(), any());
        verify(powerUserService, never()).replacePowerUsers(any()); // 빈 리스트일 때는 호출되지 않음
    }
}
