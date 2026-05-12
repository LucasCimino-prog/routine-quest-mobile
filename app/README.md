# Routine Quest - Mobile

Este é o módulo mobile do projeto **Routine Quest**, desenvolvido como Trabalho de Conclusão de Curso (TCC) em Ciência da Computação na UNIPAC Barbacena. O aplicativo gamifica rotinas diárias e integra-se a um backend Spring Boot para gestão de usuários e atividades.

## Funcionalidades Atuais
- **Cadastro de Usuários:** Interface para registro de novos jogadores.
- **Autenticação (Login):** Acesso seguro utilizando JWT (JSON Web Token).
- **Integração com API:** Consumo de serviços REST via Retrofit.
- **Arquitetura:** Baseada em Activities e comunicação assíncrona.

## Tecnologias Utilizadas
- **Linguagem:** Java
- **IDE:** Android Studio
- **Comunicação:** Retrofit & OkHttp
- **JSON Parsing:** GSON
- **Segurança:** SharedPreferences para persistência de Token JWT

## Como rodar o projeto
1. Clone o repositório.
2. Abra o projeto no Android Studio.
3. Certifique-se de que o [Backend (Spring Boot)](https://github.com/LucasCimino-prog/RoutineQuest.git) esteja rodando.
4. No arquivo `ApiClient.java`, ajuste o IP para o endereço da sua máquina local:
   ```java
   private static final String BASE_URL = "http://SEU_IP_AQUI:8080/";
5. conecte um dispositivo android, de preferencia com um cabo usb. 
6. rode o projeto.