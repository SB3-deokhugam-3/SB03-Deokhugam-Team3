package com.sprint.deokhugam.domain.poweruser.batch.processor;

import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PowerUserProcessor implements ItemProcessor<PowerUserData, PowerUser> {

    @Override
    public PowerUser process(PowerUserData userData) throws Exception {
        // 활동 점수 계산
        Double score = PowerUserService.calculateActivityScore(
            userData.reviewScoreSum(),
            userData.likeCount(),
            userData.commentCount()
        );

        // PowerUser 엔티티 생성
        PowerUser powerUser = new PowerUser(
            userData.user(),
            userData.period(),
            1L,
            score,
            userData.reviewScoreSum(),
            userData.likeCount(),
            userData.commentCount()
        );

        log.debug("PowerUser 처리 완료 - {}, 계산된 점수 : {} ",
            userData.getUserSummary(), powerUser.getScore());

        return powerUser;
    }
}
