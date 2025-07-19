package com.sprint.deokhugam.domain.popularbook.repository;

import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {

}
