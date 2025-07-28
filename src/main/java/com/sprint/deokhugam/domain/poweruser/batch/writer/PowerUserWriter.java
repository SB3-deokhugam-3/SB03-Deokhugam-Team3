package com.sprint.deokhugam.domain.poweruser.batch.writer;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerUserWriter implements ItemWriter<PowerUser> {

    private final PowerUserService powerUserService;

    @Override
    public void write(Chunk<? extends PowerUser> chunk) throws Exception {
        List<PowerUser> powerUsers = new ArrayList<>(chunk.getItems());

        if (powerUsers.isEmpty()) {
            log.info("저장할 PowerUser 데이터가 없습니다.");
            return;
        }

        log.info("PowerUser 배치 저장 시작 - {} 건", powerUsers.size());

        // 점수 기준으로 정렬 후 순위 재할당 및 저장
        powerUserService.replacePowerUsers(powerUsers);

        log.info("PowerUser 배치 저장 완료 - {} 건", powerUsers.size());
    }
}