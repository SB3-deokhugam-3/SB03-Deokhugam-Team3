package com.sprint.deokhugam.domain.popularbook.service;

import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;

public interface PopularBookService {

    CursorPageResponse<PopularBookDto> getPopularBooks(PopularBookGetRequest request);
}
