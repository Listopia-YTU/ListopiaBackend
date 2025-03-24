package com.savt.listopia.repository;

import com.savt.listopia.model.user.PrivateMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    Page<PrivateMessage> findAllByToUserId(Long toUserId, Pageable page);
    Page<PrivateMessage> findAllByFromUserIdAndToUserId(Long fromId, Long toId, Pageable page);
    Page<PrivateMessage> findAllByFromUserId(Long fromId, Pageable page);
    Page<PrivateMessage> findAllByIsReportedTrue(Pageable page);
    Optional<PrivateMessage> findPrivateMessageById(Long id);
}
