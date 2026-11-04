# Deploy do back-end

O back-end já tem `Dockerfile` (multi-stage, usuário não-root). Duas opções; escolha uma.

## Variáveis de ambiente (obrigatórias nas duas)

| Variável | Descrição |
|---|---|
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl` |
| `DB_USERNAME` | RM do aluno |
| `DB_PASSWORD` | senha do Oracle |
| `JWT_SECRET` | string aleatória com **32+ caracteres** |
| `ADMIN_EMAIL` | e-mail do administrador inicial (criado no startup se não existir) |
| `ADMIN_PASSWORD` | senha do administrador inicial |
| `CORS_ALLOWED_ORIGINS` | opcional; URL do front web (ex.: `https://prontpet.vercel.app`). Padrão `*` |
| `PORT` | opcional; plataformas como Render já definem |

> Atenção: confirme que o Oracle da FIAP aceita conexão vinda da nuvem escolhida
> (teste a primeira subida e leia os logs do container). Esse é o principal risco do deploy.

## Opção A — Render (mais simples)

1. Render → **New → Web Service** → conecte o repositório do GitHub.
2. **Runtime: Docker** (ele detecta o `Dockerfile`).
3. Em **Environment**, cadastre as variáveis da tabela acima.
4. **Health Check Path:** `/actuator/health`.
5. Deploy. A URL pública fica como `https://<nome>.onrender.com`.
6. Teste: `GET <url>/actuator/health` → `{"status":"UP"}` e abra `<url>/swagger-ui.html`.

O plano gratuito "dorme" após inatividade: abra a URL uns 2 minutos antes de gravar o vídeo.

## Opção B — Azure (reaproveita ACR/ACI de DevOps & Cloud)

Modelo de script (ajuste nomes/região). Rode no Azure CLI a partir da raiz do repositório.

```bash
RM=rm564232
RG=$RM-prontpet-rg
ACR=${RM}prontpetacr          # só minúsculas e números
APP=$RM-prontpet-api
LOC=canadacentral

az group create -n $RG -l $LOC
az acr create -n $ACR -g $RG --sku Basic --admin-enabled true

# build da imagem direto no ACR (sem precisar de Docker local)
az acr build -r $ACR -t prontpet-api:1.0 .

ACR_PASS=$(az acr credential show -n $ACR --query "passwords[0].value" -o tsv)

az container create -g $RG -n $APP \
  --image $ACR.azurecr.io/prontpet-api:1.0 \
  --registry-login-server $ACR.azurecr.io \
  --registry-username $ACR --registry-password "$ACR_PASS" \
  --dns-name-label $APP --ports 8080 --cpu 1 --memory 1.5 \
  --os-type Linux \
  --secure-environment-variables \
      DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl" \
      DB_USERNAME="<RM>" DB_PASSWORD="<SENHA>" \
      JWT_SECRET="<32+ caracteres>" \
      ADMIN_EMAIL="admin@prontpet.com" ADMIN_PASSWORD="<SENHA_ADMIN>"
```

URL: `http://$APP.$LOC.azurecontainer.io:8080` (Swagger em `/swagger-ui.html`).
Para ver erros: `az container logs -g $RG -n $APP`.

> O banco continua sendo o Oracle da FIAP (o app só se conecta a ele). Não suba Oracle em ACI
> com Azure Files: no checkpoint de DevOps esse combo travou.

## Front-end (React Native / Expo) — para o app ficar acessível online

O front hoje só roda local no Expo Go. Duas formas de deixá-lo acessível:

1. **Versão web (link clicável, recomendada para o avaliador):**
   ```bash
   npx expo install react-native-web react-dom @expo/metro-runtime
   npx expo export --platform web      # gera a pasta dist/
   ```
   Publique `dist/` na Vercel ou Netlify (arrastar a pasta já funciona).
   Depois coloque a URL do front em `CORS_ALLOWED_ORIGINS` no back-end.
2. **Expo Go via link (EAS Update):** `npx eas-cli update --branch main` gera um link/QR
   que qualquer pessoa com Expo Go abre.

Nas duas, troque a URL da API no config do app para a URL do deploy do back-end (HTTPS no caso da
versão web; o ACI da opção B é HTTP e navegadores bloqueiam HTTP a partir de página HTTPS — prefira
o Render, que entrega HTTPS).
