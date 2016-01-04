package io.acme.registration.app;

import io.acme.registration.bundle.RESTKeys;
import io.acme.registration.command.RegisterNewUserProfileCommand;
import io.acme.registration.model.CommandPromise;
import io.acme.registration.model.UserProfile;
import io.acme.registration.validation.UserProfileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

/**
 * The application interface responsible for handling the users profiles.
 * The class is located in the application layer
 */
@RestController
@RequestMapping(RESTKeys.Profile.BASE)
public class ProfileApplication {

    private static final Logger log = LoggerFactory.getLogger(ProfileApplication.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${command.bus.rabbit.exchange}")
    private String exchange;

    @InitBinder
    protected void initBinder(final WebDataBinder binder) {

        binder.setValidator(new UserProfileValidator());
    }


    @RequestMapping(value = RESTKeys.Profile.REGISTRATION, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommandPromise> registerProfile(@Valid @RequestBody final UserProfile profile) {
        final RegisterNewUserProfileCommand registerCommand = new RegisterNewUserProfileCommand(UUID.randomUUID(),
                profile.getUsername(), profile.getEmail());

        this.rabbitTemplate.convertAndSend(this.exchange, registerCommand.getClass().getName(), registerCommand);
        return new ResponseEntity<CommandPromise>(new CommandPromise(registerCommand.getId(), "Success!"), HttpStatus.OK);
    }
}