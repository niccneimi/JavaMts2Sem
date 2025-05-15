package com.example.actuator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RandomUuidEndpoint {

    @GetMapping("/admin/random-uuid")
    public String randomUuid() {
        return UUID.randomUUID().toString();
    }
}
