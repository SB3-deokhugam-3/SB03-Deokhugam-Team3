package com.sprint.deokhugam.domain.popularbook.repository;

import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.global.period.PeriodType;
import java.util.List;

public interface PopularBookRepositoryCustom {

    List<PopularBookDto> findAllByRequest(PopularBookGetRequest request);

    long deleteByPeriod(PeriodType period);
}
