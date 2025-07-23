package com.sprint.deokhugam.domain.popularbook.tasklet;

import com.sprint.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class DeletePopularBooksTasklet implements Tasklet {

    private final PopularBookRepository repository;
    private final PeriodType period;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        long deleted = repository.deleteByPeriod(period);

        log.info("[DeletePopularBooksTasklet] {} 기간 인기 도서 {}건 삭제 완료", period, deleted);

        return RepeatStatus.FINISHED;
    }
}
