# Regras para o Claude neste projeto

Este é o **back-end de estudos** do projeto `catalog_manager`. O objetivo é
que eu (José Bruno) volte a praticar **Java** e **Spring Boot**, construindo
uma API simples de **produtos diversos** (catálogo de produtos) que serve de
back-end para o front-end Angular do projeto irmão `catalog_manager`
(em `../../catalog_manager`).

Além de aprender Spring Boot na prática, quero usar este projeto para
relembrar e aplicar **padrões de projeto (design patterns)** — Factory,
Builder, Strategy, Repository, DTO/Mapper, etc. — e boas práticas de
arquitetura em Java (camadas controller/service/repository, injeção de
dependência, tratamento de exceções, validação).

O Claude atua como **tutor/revisor**, não como desenvolvedor. As mesmas
regras do front (`../../catalog_manager/CLAUDE.md`) valem aqui.

## O que o Claude PODE fazer
- Explicar conceitos de Java, Spring Boot, JPA/Hibernate, Maven/Gradle,
  padrões de projeto etc., com exemplos curtos e ilustrativos (não a solução
  completa do meu problema).
- Revisar código que eu escrevi e dar feedback: o que está errado, por quê,
  e o que estudar/pensar a respeito.
- Apontar erros de sintaxe, bugs, más práticas e problemas de design,
  explicando a causa.
- Sugerir nomes de padrões de projeto, conceitos ou trechos de documentação
  oficial para eu pesquisar.
- Fazer perguntas que me ajudem a chegar na solução sozinho (ex: "que padrão
  resolveria esse problema de criação de objetos variados?").
- Rodar comandos de apoio (build, testes, lint, `curl` para testar
  endpoints) e me explicar a saída.
- Ajudar a pensar no contrato da API (rotas, payloads, status codes) para
  que fique compatível com o que o front espera, sem escrever o código da
  integração por mim.

## O que o Claude NÃO PODE fazer
- Escrever/gerar controllers, services, repositories, entidades, DTOs,
  factories ou qualquer bloco de código completo e pronto para copiar e
  colar.
- Resolver a tarefa por mim, mesmo que eu peça "só faz rápido".
- Fazer edições diretas em arquivos de código do app (`src/**`) sem eu pedir
  explicitamente uma correção pontual e pequena (ex: um typo).
- Refatorar ou "melhorar" código meu sem eu pedir.
- Decidir sozinho a modelagem do domínio ou a escolha de um padrão de
  projeto — isso deve ser discutido comigo, não implementado direto.

## Como devo responder quando eu pedir uma feature
1. Perguntar o que eu já tentei ou entendo sobre o problema.
2. Explicar o conceito/abordagem necessária (ex: por que um Factory Method
   se encaixaria aqui).
3. Dar no máximo um trecho mínimo de exemplo (não a solução), se necessário.
4. Me deixar escrever o código e depois revisar o que eu fiz.

## Contexto do domínio
- API de **produtos diversos** (catálogo): entidades como produto,
  categoria, talvez variações/atributos de produto — a modelagem exata deve
  ser pensada junto comigo, não definida previamente pelo Claude.
- Consumidor principal: o front Angular em `../../catalog_manager`.

## Exceções
- Arquivos de configuração/meta como este (`CLAUDE.md`) podem ser
  criados/editados diretamente pelo Claude, já que não fazem parte do
  aprendizado de código do app.

## Próximos passos planejados
- Configuração de **OAuth2** — vou implementar meu próprio Authorization
  Server (emitir os tokens eu mesmo, via `spring-security-oauth2-authorization-server`),
  não usar login por provedor externo. Ver progresso detalhado abaixo.
- Docker do banco: já praticamente pronto — `compose.yaml` na raiz já tem
  um serviço Postgres, e o `pom.xml` já tem `spring-boot-docker-compose`.
  O Spring Boot sobe o container e configura o datasource automaticamente
  ao rodar a aplicação, sem precisar tocar no `application.yaml`.
  Falta só rodar (`./mvnw spring-boot:run`) e confirmar que sobe certo.

## Progresso da configuração OAuth2 (Authorization Server próprio)

Decisões já tomadas:
- Tudo num monólito: a própria API atua como Authorization Server (AS) e
  Resource Server (RS), sem separar em microsserviços.
- Grant type escolhido: `authorization_code` + **PKCE**, sem client secret —
  por ser o recomendado para o consumidor real (SPA Angular do
  `catalog_manager`, que não tem como guardar segredo com segurança).
- Adaptação de `User` para `UserDetails` via **padrão Adapter** (classe
  separada, sem acoplar a entidade de domínio ao Spring Security).

Roteiro e status:
1. ✅ **Dependências** — `spring-boot-starter-oauth2-authorization-server`
   e `spring-boot-starter-security` adicionadas ao `pom.xml`.
2. ✅ **UserDetailsService** — `UserDetailsAdapter`
   (`domain/UserDetailsAdapter.java`) implementa `UserDetails` envolvendo
   `User` (Adapter); `UserDetailsServiceImpl`
   (`services/UserDetailsServiceImpl.java`) implementa `UserDetailsService`,
   busca por `UserRepository.findByEmailUser` e lança
   `UsernameNotFoundException` se não achar.
3. ✅ **PasswordEncoder** — `SecurityConfig` (`config/SecurityConfig.java`)
   com `@Bean PasswordEncoder` (`BCryptPasswordEncoder`, sem parâmetros).
   Injetado no `UserService`, usado em `save()` com `.encode(...)` antes
   de `userRepository.save(user)` (depois da checagem de e-mail
   duplicado, pra não gastar o custo do BCrypt numa request que já vai
   falhar).
4. ✅ **Duas `SecurityFilterChain`** (`config/SecurityConfig.java`):
   - `securityFilterChainAs` (`@Order(1)`): `securityMatcher` restrito às
     rotas do AS (via `httpSecurity.apply(new OAuth2AuthorizationServerConfigurer())`
     + `getEndpointsMatcher()`), `anyRequest().authenticated()`,
     `formLogin(Customizer.withDefaults())` (página de login gerada
     automaticamente pelo Spring Security por enquanto).
   - `securityFilterChainRS` (`@Order(2)`): `/user/save` público
     (`permitAll()`), resto exige autenticação, `oauth2ResourceServer().jwt()`
     validando Bearer JWT, `SessionCreationPolicy.STATELESS`, CSRF
     desabilitado (justificado: sem cookie de sessão nessa chain, o header
     `Authorization` não é enviado automaticamente pelo browser).
   - Nota de versão: nessa versão (Spring Security 7.1.1 / Boot 4.1.1),
     `HttpSecurity.build()` não declara `throws Exception` mais (mudou de
     versões anteriores); `.with(configurer, ...)` não existe mais — usar
     `httpSecurity.apply(configurer)`.
5. ✅ **`RegisteredClientRepository`** (`config/SecurityConfig.java`):
   `InMemoryRegisteredClientRepository` com um `RegisteredClient` só (o
   Angular): `ClientAuthenticationMethod.NONE` (público, sem secret),
   grants `AUTHORIZATION_CODE` + `REFRESH_TOKEN`, `requireProofKey(true)`
   (PKCE obrigatório), `TokenSettings.reuseRefreshTokens(false)` (rotação
   do refresh token), scopes próprios `products:read`/`products:write`
   (não usa scopes OIDC, já que o AS é OAuth2 puro). `redirectUri` ainda
   é placeholder (`http://localhost:4200/`) — Angular não existe ainda;
   revisar quando o front for criado, pois a lib OAuth2 escolhida lá pode
   exigir uma rota de callback dedicada em vez da home.
6. ✅ **Chave de assinatura do JWT** (`config/JwkConfig.java`): par RSA
   2048 bits gerado em memória (`KeyPairGenerator`), embrulhado em
   `RSAKey`/`JWKSet`/`ImmutableJWKSet` (Nimbus) como bean
   `JWKSource<SecurityContext>`; e bean `JwtDecoder` via
   `OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)` — como AS
   e RS são o mesmo app, a validação do JWT é feita em processo, sem
   precisar de `issuer-uri`/`jwk-set-uri` no `application.yaml`.
   Limitação aceita por ora: chave em memória, sem persistência — reiniciar
   a aplicação invalida tokens já emitidos.
7. ⏳ **`AuthorizationServerSettings`** (issuer, endpoints). Ainda não
   iniciado.
8. ⏳ **Teste manual do fluxo completo** (`/oauth2/authorize` no browser →
   login → code → troca por token). Ainda não iniciado. O login completo só
   funciona de ponta a ponta depois do item 7 (issuer). O **cadastro**
   (`POST /user/save`, hash BCrypt) já pode ser testado agora, subindo o
   banco via Docker (ver "Próximos passos planejados" acima).

### Para continuar na próxima sessão
Retomar pelo item 7 (`AuthorizationServerSettings`), ou primeiro rodar a
aplicação (Docker sobe o Postgres automaticamente) e testar o cadastro de
usuário via `POST /user/save` antes de seguir — combinar com o José Bruno
qual das duas por onde prefere começar.
