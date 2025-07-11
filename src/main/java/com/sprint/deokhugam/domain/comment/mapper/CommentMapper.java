package com.sprint.deokhugam.domain.comment.mapper;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

//    @Mapping(target ="reviewId", source = "review.id")
//    @Mapping(target = "userId", source = "user.id")
    CommentDto toDto(Comment comment);

}
