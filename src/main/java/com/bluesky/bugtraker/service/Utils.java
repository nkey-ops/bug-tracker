package com.bluesky.bugtraker.service;

import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Service
public class Utils {


    private final Random RANDOM = new SecureRandom();
    private final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public String generateUserId(int length) {
        return generateRandomString(length);
    }

    public String generateProjectId(int length) {
        return generateRandomString(length);
    }

    public String generateTicketId(int length) {
        return generateRandomString(length);
    }

    public String generateCommentId(int length) {
        return generateRandomString(length);
    }

    public String generateTicketRecordId(int length) {
        return generateRandomString(length);
    }

    private String generateRandomString(int length) {
        StringBuilder returnValue = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }

        return new String(returnValue);
    }


    public <T> DataTablesOutput<T> convert(DataTablesOutput<?> source, Class<T> clazz) {
        ModelMapper modelMapper = new ModelMapper();

        DataTablesOutput<T> result = new DataTablesOutput<>();

        modelMapper.map(source, result);

        result.setData(modelMapper.map(source.getData(), new TypeToken<List<UserResponseModel>>() {
        }.getType()));

        return result;
    }
}