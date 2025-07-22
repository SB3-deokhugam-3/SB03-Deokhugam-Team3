package com.sprint.deokhugam.domain.popularbook.repository;

import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.global.period.PeriodType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PopularBookRepository extends JpaRepository<PopularBook, UUID>, PopularBookRepositoryCustom {

    long countByPeriod(PeriodType period);
}
