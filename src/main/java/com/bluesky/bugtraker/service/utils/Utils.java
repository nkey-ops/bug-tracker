package com.bluesky.bugtraker.service.utils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bluesky.bugtraker.security.SecurityConstants;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.constraints.NotNull;

@Service
public class Utils {
  private final Random RANDOM = new SecureRandom();
  private final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public Utils(BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  public String generateUserId() {
    return generateRandomString(15);
  }

  public String generateProjectId() {
    return generateRandomString(15);
  }

  public String generateRandomString(int length) {
    StringBuilder returnValue = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
    }

    return new String(returnValue);
  }

  public String encode(String password) {
    return bCryptPasswordEncoder.encode(password);
  }

  public <T> DataTablesOutput<T> map(
      @NotNull DataTablesOutput<?> source, TypeToken<List<T>> listTypeToken) {
    ModelMapper modelMapper = new ModelMapper();

    DataTablesOutput<T> result = new DataTablesOutput<>();
    modelMapper.map(source, result);

    result.setData(modelMapper.map(source.getData(), listTypeToken.getType()));

    return result;
  }

  public boolean hasEmailTokenExpired(@NotNull String token) {
    return Jwts.parser()
        .verifyWith(
            new SecretKeySpec(
                Base64.encodeBase64URLSafeString(SecurityConstants.getTokenSecret().getBytes())
                    .getBytes(),
                SignatureAlgorithm.HS256.getJcaName()))
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getExpiration()
        .before(new Date());
  }

  public String getEmailVerificationToken(String publicId) {
    return Jwts.builder()
        .setSubject(publicId)
        .setExpiration(Date.from(Instant.now().plusMillis(SecurityConstants.EXPIRATION_TIME)))
        .signWith(
            SignatureAlgorithm.HS256,
            Base64.encodeBase64URLSafeString(SecurityConstants.getTokenSecret().getBytes())
                .getBytes())
        .compact();
  }
}
