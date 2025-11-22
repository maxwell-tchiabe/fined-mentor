package com.fined.mentor.auth.repository;

import com.fined.mentor.auth.entity.Token;
import com.fined.mentor.auth.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
    Optional<Token> findByTokenAndType(String token, Token.TokenType type);
    Optional<Token> findByUserAndType(User user, Token.TokenType type);
    void deleteByUserAndType(User user, Token.TokenType type);
}