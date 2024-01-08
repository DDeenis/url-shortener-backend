package com.itstep.coursework.services.kdf;

public interface KdfService {
    String getDerivedKey(String password, String salt);
}
