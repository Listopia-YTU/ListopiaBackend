package com.savt.listopia.service.user;

import com.savt.listopia.exception.ResourceNotFoundException;
import com.savt.listopia.exception.userException.UserNotFoundException;
import com.savt.listopia.model.user.PrivateMessage;
import com.savt.listopia.model.user.User;
import com.savt.listopia.payload.dto.PrivateMessageDTO;
import com.savt.listopia.repository.PrivateMessageRepository;
import com.savt.listopia.repository.UserRepository;
import com.savt.listopia.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserMessageServiceImpl implements UserMessageService {
    private final UserRepository userRepository;
    private final PrivateMessageRepository privateMessageRepository;
    private final NotificationService notificationService;

    public UserMessageServiceImpl(UserRepository userRepository, PrivateMessageRepository privateMessageRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.privateMessageRepository = privateMessageRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void userReportMessage(Long userId, Long messageId) {
        PrivateMessage msg = privateMessageRepository.findById(messageId).orElseThrow(() -> new UserNotFoundException("message_does_not_exists"));
        if (msg.getToUser().getId().equals(userId)) {
            markPrivateMessageReported(messageId);
        }
    }

    @Transactional
    public void sendMessage(Long fromId, Long toId, String messageStr) {
        User sender = userRepository.findById(fromId).orElseThrow(UserNotFoundException::new);
        User receiver = userRepository.findById(toId).orElseThrow(UserNotFoundException::new);
        PrivateMessage message = new PrivateMessage();
        message.setFromUser( sender );
        message.setToUser( receiver );
        message.setSentAtTimestampSeconds(Instant.now().getEpochSecond());
        message.setMessage(messageStr);
        notificationService.notifyNewMessage(sender, receiver, messageStr);
        privateMessageRepository.save(message);
    }

    @Transactional
    public void markPrivateMessageReported(Long messageId) {
        PrivateMessage msg = privateMessageRepository.findById(messageId).orElseThrow(ResourceNotFoundException::new);
        msg.setIsReported(true);
        privateMessageRepository.save(msg);
    }

    public Boolean isPrivateMessageReported(Long messageId) {
        return privateMessageRepository.findPrivateMessageById(messageId).orElseThrow(ResourceNotFoundException::new).getIsReported();
    }

    public Page<PrivateMessageDTO> getAllReportedMessages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByIsReportedTrue(pageable)
                .map(this::privateMessageToDTO);
    }

    private PrivateMessageDTO privateMessageToDTO(PrivateMessage message) {
        PrivateMessageDTO dto = new PrivateMessageDTO();
        dto.setId(message.getId());
        // dto.setFromUserUUID(getUUIDFromUserId(message.getFromUserId()).toString());
        dto.setFromUserUUID( message.getFromUser().getUuid().toString() );
        // dto.setToUserUUID(getUUIDFromUserId(message.getToUserId()).toString());
        dto.setToUserUUID( message.getToUser().getUuid().toString() );
        dto.setSentAtTimestampSeconds(message.getSentAtTimestampSeconds());
        dto.setMessage(message.getMessage());
        return dto;
    }

    public Page<PrivateMessageDTO> getAllMessagesOfUserReceived(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByToUserId(userId, pageable)
                .map(this::privateMessageToDTO);
    }

    public Page<PrivateMessageDTO> getAllMessagesUserSent(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByFromUserId(userId, pageable)
                .map(this::privateMessageToDTO);
    }

    public Page<PrivateMessageDTO> getAllMessagesSentTo(Long userId, Long toId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByFromUserIdAndToUserId(userId, toId, pageable)
                .map(this::privateMessageToDTO);
    }

    public Page<PrivateMessageDTO> getAllMessagesReceivedFrom(Long userId, Long fromId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAtTimestampSeconds").descending());
        return privateMessageRepository.findAllByFromUserIdAndToUserId(fromId, userId, pageable)
                .map(this::privateMessageToDTO);
    }
}
