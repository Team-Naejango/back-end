package com.example.naejango.domain.storage.api;

import com.example.naejango.domain.storage.dto.request.CreateStorageRequestDto;
import com.example.naejango.domain.storage.dto.response.StorageInfoResponseDto;
import com.example.naejango.domain.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @GetMapping("/{id}")
    public ResponseEntity<StorageInfoResponseDto> StorageInfo (@PathVariable("id") Long storageId) {
        StorageInfoResponseDto info = storageService.info(storageId);
        return ResponseEntity.ok().body(info);
    }

    @PostMapping
    public ResponseEntity<Void> createStorage(@RequestBody CreateStorageRequestDto requestDto, Authentication authentication) {
        storageService.createStorage(requestDto, authentication);
        return ResponseEntity.ok().body(null);
    }
}