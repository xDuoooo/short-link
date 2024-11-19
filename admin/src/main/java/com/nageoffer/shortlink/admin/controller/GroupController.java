package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

}
