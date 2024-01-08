package com.itstep.coursework.services.random;

public interface RandomService {
    String randomHex(int charLength);
    void seed(String iv);
}
