package com.savt.listopia.repository;

import com.savt.listopia.model.user.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    List<PrivateMessage> findAllByToUserId(Long toUserId);
    List<PrivateMessage> findAllByFromUserIdAndToUserId(Long fromId, Long toId);
    List<PrivateMessage> findAllByFromUserId(Long fromId);
    List<PrivateMessage> findAllByIsReportedTrue();
    Optional<PrivateMessage> findPrivateMessageById(Long id);
}
