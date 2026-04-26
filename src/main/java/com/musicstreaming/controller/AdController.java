package com.musicstreaming.controller;

import com.musicstreaming.dto.AdDTO;
import com.musicstreaming.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ad")
public class AdController {

    @Autowired
    private AdService adService;

    @GetMapping("/should-show")
    public ResponseEntity<Map<String, Boolean>> shouldShowAd(HttpServletRequest request) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("shouldShowAd", adService.shouldShowAd(request));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/random")
    public ResponseEntity<AdDTO> getRandomAd() {
        return ResponseEntity.ok(adService.getRandomAd());
    }
}