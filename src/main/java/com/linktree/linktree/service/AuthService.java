package com.linktree.linktree.service;

import com.linktree.linktree.dto.AuthResponseDTO;
import com.linktree.linktree.dto.ForgotPasswordDTO;
import com.linktree.linktree.dto.LoginDTO;
import com.linktree.linktree.dto.RegisterDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterDTO registerDTO);
    AuthResponseDTO login(LoginDTO loginDTO);
    void forgotPassword(ForgotPasswordDTO forgotPasswordDTO);
    boolean resetPassword(String token, String newPassword);
}

