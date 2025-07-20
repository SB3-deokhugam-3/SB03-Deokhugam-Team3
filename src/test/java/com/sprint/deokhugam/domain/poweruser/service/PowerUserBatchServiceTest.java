package com.sprint.deokhugam.domain.poweruser.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.Arrays;
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
        verify(powerUserRepository).deleteByPeriod(PeriodType.DAILY);
        verify(powerUserRepository).calculateAndCreatePowerUsers(eq(PeriodType.DAILY), any(), any());
        verify(powerUserService).savePowerUsers(mockPowerUsers);
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
        verify(powerUserService).savePowerUsers(mockPowerUsers);
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
        verify(powerUserService).savePowerUsers(mockPowerUsers);
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
        verify(powerUserService).savePowerUsers(mockPowerUsers);
    }

}
