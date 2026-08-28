package com.example.exam_service.controller;

import com.example.exam_service.dto.OptionRequestDTO;
import com.example.exam_service.dto.OptionResponseDTO;
import com.example.exam_service.dto.OptionUpdateRequest;
import com.example.exam_service.service.OptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OptionController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    @PostMapping("/questions/{questionId}/options")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OptionResponseDTO> addOption(@PathVariable Long questionId,
                                                     @Valid @RequestBody OptionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(optionService.addOption(questionId, request));
    }

    @GetMapping("/questions/{questionId}/options")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OptionResponseDTO>> getOptionsByQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(optionService.getOptionsByQuestion(questionId));
    }

    @PutMapping("/options/{optionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OptionResponseDTO> updateOption(@PathVariable Long optionId,
                                                        @Valid @RequestBody OptionUpdateRequest request) {
        return ResponseEntity.ok(optionService.updateOption(optionId, request));
    }

    @DeleteMapping("/options/{optionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteOption(@PathVariable Long optionId) {
        optionService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }
}
