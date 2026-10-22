package no.oskott.towerdefbackend.run;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import no.oskott.towerdefbackend.config.TowerdefProperties;
import org.springframework.stereotype.Service;

@Service
public class RunTokenService {

    private final byte[] key;

    public RunTokenService(TowerdefProperties props) {
        this.key = props.security().secret().getBytes(StandardCharsets.UTF_8);
    }

    public String create(UUID runId, Instant startedAt) {
        return hmac(runId, startedAt);
    }

    public boolean verify(UUID runId, Instant startedAt, String token) {
        if (token == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hmac(runId, startedAt).getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
    }

    private String hmac(UUID runId, Instant startedAt) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] out = mac.doFinal((runId + "|" + startedAt.toEpochMilli()).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
