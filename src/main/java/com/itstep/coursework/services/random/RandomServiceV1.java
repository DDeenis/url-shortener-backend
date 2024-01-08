package com.itstep.coursework.services.random;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Primary
public class RandomServiceV1 implements RandomService {
    private String iv;
    private final Random random;

    public RandomServiceV1() {
        random = new Random();
    }

    @Override
    public String randomHex(int charLength) {
        if(charLength < 1) throw new RuntimeException("charLength should be positive");

        char[] hexChars = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        char[] res = new char[charLength];
        for (int i = 0; i < charLength; i++) {
            res[i] = hexChars[random.nextInt(hexChars.length)];
        }
        return new String(res);
    }

    @Override
    public void seed(String iv) {
        this.iv = iv;
        random.setSeed(iv == null ? 0 : iv.hashCode());
    }
}
