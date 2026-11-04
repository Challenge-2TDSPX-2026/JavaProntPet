package br.com.project.prontpet.controllers;

import br.com.project.prontpet.dtos.AccountRequest;
import br.com.project.prontpet.dtos.AccountResponse;
import br.com.project.prontpet.dtos.LoginRequest;
import br.com.project.prontpet.dtos.LoginResponse;
import br.com.project.prontpet.models.Account;
import br.com.project.prontpet.security.AccountUserDetails;
import br.com.project.prontpet.security.TokenService;
import br.com.project.prontpet.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final AccountService accountService;

    public AuthController(
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            AccountService accountService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.accountService = accountService;
    }

    @PostMapping("/login")
    @Operation(
            tags = "Auth",
            summary = "Login",
            description = "Recebe e-mail e senha e retorna o token JWT (validade configuravel, padrao 2h)."
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Account account = ((AccountUserDetails) authentication.getPrincipal()).getAccount();
        return ResponseEntity.ok(new LoginResponse(tokenService.generateToken(account)));
    }

    @PostMapping("/register")
    @Operation(
            tags = "Auth",
            summary = "Cadastrar conta",
            description = "ROLE_USER (tutor, com 'owner') ou ROLE_VET (clinica, com 'clinic'). ROLE_ADMIN nao pode ser criado por aqui."
    )
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody AccountRequest request) {
        Account account = accountService.addAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.fromEntity(account));
    }
}
