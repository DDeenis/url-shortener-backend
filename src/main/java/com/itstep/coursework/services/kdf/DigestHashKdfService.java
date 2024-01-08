package com.itstep.coursework.services.kdf;

import com.itstep.coursework.services.hash.HashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DigestHashKdfService implements KdfService {
    private final HashService hashService;

    @Autowired
    public DigestHashKdfService(HashService hashService) {
        this.hashService = hashService;
    }

    @Override
    public String getDerivedKey(String password, String salt) {
        String dk = hashService.hash(salt + password + salt);
        int maxLength = 64;
        return dk.length() > maxLength ? dk.substring(0, maxLength) : dk;
    }
}
