package it.unisalento.recproject.recprojectio.restcontrollers;
import it.unisalento.recproject.recprojectio.di.IRenewableSource;
import it.unisalento.recproject.recprojectio.domain.User;
import it.unisalento.recproject.recprojectio.dto.LoginDTO;
import it.unisalento.recproject.recprojectio.dto.UserDTO;
import it.unisalento.recproject.recprojectio.dto.AuthenticationResponseDTO;
import it.unisalento.recproject.recprojectio.dto.UsersListDTO;
import it.unisalento.recproject.recprojectio.exceptions.UserNotFoundException;
import it.unisalento.recproject.recprojectio.repositories.UserRepository;
import it.unisalento.recproject.recprojectio.security.JwtUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    IRenewableSource footballTeam;

    @Autowired
    IRenewableSource basketTeam;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtilities jwtUtilities;

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public UserDTO get(@PathVariable String id) throws UserNotFoundException {

        footballTeam.initialize();
        basketTeam.initialize();

        Optional<User>user = userRepository.findById(id);
        if(user.isEmpty()){
            throw new UserNotFoundException();
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.get().getId());
        userDTO.setName(user.get().getName());
        userDTO.setEmail(user.get().getEmail());

        return userDTO; //DTO
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public UsersListDTO getAll(){

        UsersListDTO usersList = new UsersListDTO();
        ArrayList<UserDTO> users = new ArrayList<>();
        usersList.setList(users);

        List<User> userList = userRepository.findAll();

        for(User user : userList){
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            userDTO.setName(user.getName());
            userDTO.setPassword(user.getPassword());
            users.add(userDTO);
        }
        return usersList;
    }
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public UsersListDTO search(@RequestParam (required = false) String name){
        UsersListDTO usersListDTO = new UsersListDTO();
        ArrayList<UserDTO> users = new ArrayList<>();
        usersListDTO.setList(users);

        List<User> list = userRepository.findByName(name);
        for(User user : list){
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            userDTO.setName(user.getName());
            users.add(userDTO);
        }
        return usersListDTO;

    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO) throws UserNotFoundException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null)
        {
            throw new UserNotFoundException();
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String jwt = jwtUtilities.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthenticationResponseDTO(jwt));
    }
}
