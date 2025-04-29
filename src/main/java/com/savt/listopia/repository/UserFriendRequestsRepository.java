package com.savt.listopia.repository;

import com.savt.listopia.model.user.User;
import com.savt.listopia.model.user.UserFriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFriendRequestsRepository extends JpaRepository<UserFriendRequest, Long> {
    Optional<UserFriendRequest> findByUserRequestSentAndUserRequestReceived(User id, User id1);

    Page<UserFriendRequest> findByUserRequestReceived(User userRequestReceived, Pageable pageable);

    Page<UserFriendRequest> findByUserRequestReceivedAndActive(User user, boolean b, Pageable pageable);

    Page<UserFriendRequest> findByUserRequestSentAndActive(User userRequestSent, Boolean active);

    Page<UserFriendRequest> findByUserRequestSentAndActive(User userRequestSent, Boolean active);

    Page<UserFriendRequest> findByUserRequestSentAndActive(User userRequestSent, Boolean active, Pageable pageable);
}
