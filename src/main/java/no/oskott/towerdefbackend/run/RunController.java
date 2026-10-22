package no.oskott.towerdefbackend.run;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import no.oskott.towerdefbackend.config.ClientIp;
import no.oskott.towerdefbackend.run.RunDtos.ScoreRequest;
import no.oskott.towerdefbackend.run.RunDtos.ScoreResponse;
import no.oskott.towerdefbackend.run.RunDtos.StartResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final RunService service;
    private final ClientIp clientIp;

    public RunController(RunService service, ClientIp clientIp) {
        this.service = service;
        this.clientIp = clientIp;
    }

    @PostMapping
    public StartResponse start(HttpServletRequest request) {
        return service.start(clientIp.hash(clientIp.resolve(request)));
    }

    @PostMapping("/{runId}/score")
    public ScoreResponse submit(@PathVariable UUID runId, @Valid @RequestBody ScoreRequest body) {
        return service.submit(runId, body);
    }
}
