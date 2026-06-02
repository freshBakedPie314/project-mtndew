package com.enigma.projectmtndew.controllers;

import com.enigma.projectmtndew.dtos.OcrReceiptResponseDTO;
import com.enigma.projectmtndew.services.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("ocr")
public class OcrControleer {

    @Autowired
    private OcrService geminiService;

//    @GetMapping("/ask")
//    public ResponseEntity<String> ask(@RequestParam String prompt) throws Exception {
//        String response = geminiService.ask(prompt);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/scan")
    public ResponseEntity<OcrReceiptResponseDTO> ask(@RequestParam("image") MultipartFile image) throws Exception {
        OcrReceiptResponseDTO response = geminiService.scanImage(image);
        return ResponseEntity.ok(response);
    }
}
