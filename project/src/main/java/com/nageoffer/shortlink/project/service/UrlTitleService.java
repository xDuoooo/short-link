package com.nageoffer.shortlink.project.service;

import org.springframework.stereotype.Service;

import java.io.IOException;

public interface UrlTitleService {
    String getTitleByUrl(String url);
}
