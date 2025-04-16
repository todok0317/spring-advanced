package org.example.expert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    @NotBlank
    private String oldPassword;

    // Lv1-3 : 코드 개선 퀴즈 - Validation
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, message = "새 비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(regexp = ".*\\d.*", message = "새 비밀번호에는 최소 하나의 숫자가 포함되어야 합니다.")
    @Pattern(regexp = ".*[A-Z].*", message = "새 비밀번호에는 최소 하나의 대문자가 포함되어야 합니다.")
    private String newPassword;
}
