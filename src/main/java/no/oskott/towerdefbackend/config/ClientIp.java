package no.oskott.towerdefbackend.config;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class ClientIp {

    private final TowerdefProperties props;

    public ClientIp(TowerdefProperties props) {
        this.props = props;
    }

    /** Only trusts X-Forwarded-For when explicitly configured (i.e. behind a known reverse proxy). */
    public String resolve(HttpServletRequest request) {
        if (props.security().trustForwardedFor()) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                String[] parts = xff.split(",");
                return parts[parts.length - 1].trim();
            }
        }
        return request.getRemoteAddr();
    }

    public String hash(String ip) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(props.security().secret().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(md.digest(ip.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
