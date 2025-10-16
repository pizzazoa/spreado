package com.example.spreado.domain.user.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "회원 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/user/")
public class UserController {
}
