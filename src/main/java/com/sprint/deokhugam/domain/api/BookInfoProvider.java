package com.sprint.deokhugam.domain.api;

import com.sprint.deokhugam.domain.api.dto.NaverBookDto;

public interface BookInfoProvider {

    NaverBookDto fetchInfoByIsbn(String isbn);
}
