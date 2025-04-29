package com.savt.listopia.service.user;

import com.savt.listopia.payload.dto.PrivateMessageDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface UserMessageService {
    void userReportMessage(Long userId, Long messageId);
    void sendMessage(Long fromId, Long toId, String messageUnsafe);
    void markPrivateMessageReported(Long messageId);
    Boolean isPrivateMessageReported(Long messageId);
    Page<PrivateMessageDTO> getAllReportedMessages(int page, int size);
    Page<PrivateMessageDTO> getAllMessagesOfUserReceived(Long userId, int page, int size);
    Page<PrivateMessageDTO> getAllMessagesUserSent(Long userId, int page, int size);
    Page<PrivateMessageDTO> getAllMessagesSentTo(Long userId, Long toId, int page, int size);
    Page<PrivateMessageDTO> getAllMessagesReceivedFrom(Long userId, Long fromId, int page, int size);
}
