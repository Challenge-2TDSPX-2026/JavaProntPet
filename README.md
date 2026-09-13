# ProntPet

**Integrantes:**  
- Anthony de Souza Henriques — RM 566188  
- Guilherme Santos Fonseca — RM 564232  
- Gustavo Araujo da Silva — RM 566526  

## Repositório

- GitHub Back-End:  
  https://github.com/Challenge-2TDSPX-2026/JavaProntPet

- GitHub Front-End: 
  https://github.com/Challenge-2TDSPX-2026/MobileProntPet

- Vídeo:
  https://youtu.be/y2-7rWh3wTE

# ProntPet

App de gestão veterinária — tutores cadastram pets e agendam consultas; clínicas gerenciam o atendimento.

**Stack:** Java 17 + Spring Boot 4 + Spring Security (JWT) + Oracle + Flyway | Frontend: React Native (Expo)

---

## Backend

### Pré-requisitos
- JDK 17, Maven, IntelliJ
- Acesso ao Oracle da FIAP (usuário/senha do RM)

### Variáveis de ambiente
Configurar no Run Configuration da IDE:

| Variável | Exemplo |
|---|---|
| `name` | RM do aluno |
| `password` | senha do Oracle |
| `url` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl` |
| `senha` | string aleatória com 32+ caracteres (chave do JWT) |

### Rodar
1. Configura as variáveis acima
2. Roda `ProntpetApplication` (ou `./mvnw spring-boot:run`)
3. O Flyway cria as tabelas sozinho na primeira execução
4. Sobe em `http://localhost:8080`

**Erro comum:** se aparecer `"Found non-empty schema... use baseline()"` (schema Oracle compartilhado com outra matéria), adiciona no `application.properties`:
```properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
```

### Acesso
- Swagger: `http://localhost:8080/swagger-ui.html`
- `POST /auth/register` → `role: ROLE_USER` (tutor, com `owner`) ou `ROLE_VET` (clínica, com `clinic`)
- `POST /auth/login` → retorna token JWT
- No Swagger: **Authorize** → cola `Bearer <token>`

### Perfis
- **ROLE_USER**: gerencia seus próprios pets e consultas
- **ROLE_VET**: gerencia consultas da própria clínica
- **ROLE_ADMIN**: acesso total

---

## Frontend

```bash
npm install
npx expo start
```

Configura a URL da API no arquivo de config do app: `10.0.2.2:8080` (emulador Android) ou o IP local da máquina (dispositivo físico). Abre com Expo Go (QR code) ou emulador.

---

## Observação técnica
Schema controlado 100% pelo Flyway (`db/migration/`) — Hibernate só valida (`ddl-auto=validate`), nunca cria/altera tabela. Mudança de estrutura = nova migration (`V2`, `V3`...), nunca editar uma já aplicada.

