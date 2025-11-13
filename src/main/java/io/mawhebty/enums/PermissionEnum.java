package io.mawhebty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum PermissionEnum {

    READ_POSTS(1, "READ_POSTS"),
    READ_PROFILE(2, "READ_PROFILE"),
    READ_FEEDS(3, "READ_FEEDS"),
    VIEW_TALENTS(4, "VIEW_TALENTS"),
    VIEW_RESEARCHERS(5, "VIEW_RESEARCHERS"),
    LIKE(6, "LIKE"),
    COMMENT(7, "COMMENT"),
    FOLLOW(8, "FOLLOW"),
    SAVE_POSTS(9, "SAVE_POSTS"),
    MANAGE_TALENT_PROFILE(10, "MANAGE_TALENT_PROFILE"),
    RECEIVE_CONTRACTS(11, "RECEIVE_CONTRACTS"),
    UPLOAD_CONTENT(12, "UPLOAD_CONTENT"),
    MANAGE_RESEARCHER_PROFILE(13, "MANAGE_RESEARCHER_PROFILE"),
    SEND_CONTRACTS(14, "SEND_CONTRACTS"),
    SEARCH(15, "SEARCH"),
    EDIT_TALENT_SHARED_CONTENT(16, "SEARCH"),
    BOOST_LISTINGS(17, "BOOST_LISTINGS");

    private Integer id;
    private String name;
}
