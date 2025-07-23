package com.sprint.deokhugam.domain.popularbook.repository;

import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.List;

public interface PopularBookRepositoryCustom {

    List<PopularBookDto> findAllByRequest(PopularBookGetRequest request);

    long deleteByPeriod(PeriodType period);
}
