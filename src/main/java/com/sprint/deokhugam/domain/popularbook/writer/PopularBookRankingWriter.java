package com.sprint.deokhugam.domain.popularbook.writer;

import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.domain.popularbook.repository.PopularBookRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class PopularBookRankingWriter implements ItemWriter<PopularBook> {

    private final PopularBookRepository repository;

    @Override
    public void write(Chunk<? extends PopularBook> chunk) {
        List<? extends PopularBook> rankingList = chunk.getItems();

        if (rankingList.isEmpty()) {
            log.info("[PopularBookRankingWriter] rankingList가 비어 있습니다.");
            return;
        }

        try {
            // 랭킹 부여
            for (int i = 0; i < rankingList.size(); i++) {
                rankingList.get(i).updateRank((long) (i + 1));
            }

            repository.saveAll(rankingList);
            log.info("[PopularBookRankingWriter] 인기 도서 {} 건 저장 완료", rankingList.size());
        } catch (Exception e) {
            log.error("인기 도서 랭킹 저장 실패", e);
            throw e;
        }
    }
}
