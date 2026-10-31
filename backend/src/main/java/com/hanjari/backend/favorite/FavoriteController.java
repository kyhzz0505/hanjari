package com.hanjari.backend.favorite;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hanjari.backend.auth.LoginSession;
import com.hanjari.backend.restaurant.RestaurantSummaryResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

// 단골 목록 API. 모두 로그인이 필요하다 (안 했으면 401)
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public List<RestaurantSummaryResponse> list(HttpServletRequest request) {
        return favoriteService.list(LoginSession.requireUserId(request));
    }

    @PutMapping("/{restaurantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void add(@PathVariable("restaurantId") Long restaurantId, HttpServletRequest request) {
        favoriteService.add(LoginSession.requireUserId(request), restaurantId);
    }

    @DeleteMapping("/{restaurantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable("restaurantId") Long restaurantId, HttpServletRequest request) {
        favoriteService.remove(LoginSession.requireUserId(request), restaurantId);
    }
}
