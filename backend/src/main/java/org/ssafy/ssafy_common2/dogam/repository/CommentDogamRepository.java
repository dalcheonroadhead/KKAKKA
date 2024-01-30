package org.ssafy.ssafy_common2.dogam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ssafy.ssafy_common2.dogam.entity.CommentDogam;

import java.util.List;

public interface CommentDogamRepository extends JpaRepository<CommentDogam,Long> {
    List<CommentDogam> findAllByDogamIdAndDeletedAtIsNull(Long id);

    CommentDogam findFirstByDogamIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long id);
}
