package br.com.project.prontpet.controllers;

import br.com.project.prontpet.dtos.LoginRequest;
import br.com.project.prontpet.dtos.LoginResponse;
import br.com.project.prontpet.models.Account;
import br.com.project.prontpet.repositories.AccountRepository;
import br.com.project.prontpet.security.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final TokenService tokenService;

    public AuthController(
            AuthenticationManager authenticationManager,
            AccountRepository accountRepository,
            TokenService tokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.accountRepository = accountRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        Account account =
                accountRepository
                        .findByEmail(authentication.getName())
                        .orElseThrow();

        String token =
                tokenService.generateToken(account);

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }
}