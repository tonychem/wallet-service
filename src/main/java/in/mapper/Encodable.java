package in.mapper;

import org.mapstruct.Named;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface Encodable {
    @Named("encodePassword")
    static byte[] encodePassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            return messageDigest.digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
