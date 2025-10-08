package com.example.demo.userManagements.serviceImplementation;

import com.example.demo.communication.services.CommunicationService;
import com.example.demo.system.parsitence.models.AuthResponse;
import com.example.demo.userManagements.parsitence.enitities.Roles;
import com.example.demo.userManagements.parsitence.enitities.Users;
import com.example.demo.userManagements.parsitence.enitities.rolePermissions;
import com.example.demo.userManagements.parsitence.models.Security;
import com.example.demo.userManagements.parsitence.models.login;
import com.example.demo.userManagements.parsitence.models.loginHistory;
import com.example.demo.userManagements.parsitence.repositories.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Log4j2
public class UserService implements UserDetailsService {
  public final userRepo userRepo;
  public final securityRepo securityRepo;
  public final loginsRepo loginsRepo;
  public final rolesRepo roles;
  public final permissionsRepo permissions;
  public final CommunicationService communication;

  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    public UserService(com.example.demo.userManagements.parsitence.repositories.userRepo userRepo, com.example.demo.userManagements.parsitence.repositories.securityRepo securityRepo, com.example.demo.userManagements.parsitence.repositories.loginsRepo loginsRepo, rolesRepo roles, permissionsRepo permissions, CommunicationService communication) {
        this.userRepo = userRepo;
        this.securityRepo = securityRepo;
        this.loginsRepo = loginsRepo;
        this.roles = roles;
        this.permissions = permissions;
        this.communication = communication;
    }
      public String randomString(){
          log.info("Generating a random string.");
          String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
          SecureRandom random = new SecureRandom();
          StringBuilder sb = new StringBuilder(12);
          for (int i = 0; i < 12; i++) {
              sb.append(chars.charAt(random.nextInt(chars.length())));
          }
          return sb.toString();
      }
    public List<Users> saveUser(Users user) {
        log.info("Creating user: "+user.getUserName());
        userRepo.save(user);
        String userid=findByName(user.getUserName()).get().getId().toString();

            log.info("User created.");
         Security security=new Security();
        log.info("Generating initial password for user: "+user.getUserName());
         security.setUserId(userid);
         security.setActive(Boolean.TRUE);
         security.setStatus("NEW");
         security.setStartDate(LocalDate.now());
        String initialPassword=randomString();
        security.setPassword(initialPassword);
          createP(security);
        log.info("Password saved.");
        String variable[] = new String[]{
                user.getEmail(),user.getFirstName(),user.getUserName(),initialPassword
        };
        log.info("Redirecting to email services.");
        try {
            communication.getData(variable);
        }catch (Exception e){
            log.warn("Error sending email: {}",e.getMessage());
        }

        return getAll();

    }
    public Users updateUser(Users user) {
        log.info("Updating user: "+user.getUserName());
        //validation
        return userRepo.save(user);
    }
    public List<Users> getAll() {
        return userRepo.findAll();
    }
    public Optional<Users> findByEmail(String email) {
        return userRepo.findByemail(email);
    }
    public Optional<Users> findById(Long id) {
        return userRepo.findById(id);
    }
    public Optional<Users> findByName(String userName) {
        return userRepo.findByuserName(userName);
    }
    public Security createP(Security security) {
        String password =security.getPassword();
        String encodedString =encode(password);
        security.setPassword(encodedString);
            return securityRepo.save(security);
    }
    public AuthResponse updateP(Security security) {
        AuthResponse responseModel=new AuthResponse();
        String password =security.getPassword();
        String encodedString =encode(password);
        security.setPassword(encodedString);
        responseModel.setHttpStatus(HttpStatus.OK);
        responseModel.setHttpStatusCode(200);
        responseModel.setMessage("Password was reset");
        securityRepo.save(security);
        return responseModel;
    }
   // encoder
 public String encode(String plainText){

     String encodedString =bCryptPasswordEncoder().encode(plainText);
            // Base64.getEncoder().encodeToString(plainText.getBytes());
     return encodedString;
 }
 //decoder
 public BCryptPasswordEncoder bCryptPasswordEncoder() {
     return new BCryptPasswordEncoder();
 }
 public String decode(String encodedText){

     byte[] decodedBytes = Base64.getDecoder().decode(encodedText);
     String decodedString = new String(decodedBytes);
     return decodedString;
 }

    public loginHistory createHistory(loginHistory history) {

        //
        return loginsRepo.save(history);
    }

    public Security findPassword(String id) {

        Security user=securityRepo.findByuserId(id).get();
        String decodedString = user.getPassword();
        user.setPassword(decodedString);
        return user;

    }
    //Enable Disable a user
    public AuthResponse changeUserStatus(Long id, Boolean status){
        AuthResponse response=new AuthResponse();
        response.setHttpStatus(HttpStatus.OK);
        response.setReason("Status update");
        response.setHttpStatusCode(200);
        Users user=findById(id).get();
        if (user.getActive()!=status){
            user.setActive(status);
            if (status==Boolean.TRUE){
                response.setMessage("Account Have been enabled please proceed");
                log.info("Enabling user "+user.getUserName());
                log.info("Changing password");
                String newpassword=randomString();
                String mailSubject="ACCOUNT ACTIVATION";
                changePassword(user.getId().toString(),newpassword);
                String[] mail=new String[]{
                        user.getEmail(),mailSubject,"Hi "+user.getFirstName()+"\r\n" +
                        " Your account have been activated \r\nPlease use Username:<b>"+user.getUserName()
                        +"</b> and Password:<b>"+newpassword+"</b> to login"
                };
                communication.sendEmail(mail);


            }else {
                response.setMessage("Account Have been disabled please proceed");
                log.info("Disabling user "+user.getUserName());
                //send email.
                String mailSubject="ACCOUNT DISABLING";
                String[] mail=new String[]{
                        user.getEmail(),mailSubject,"Hi "+user.getFirstName()+"\r\n" +
                        " Your account have been Temporarily Disabled Please contact your Administrator for more information"
                };
                communication.sendEmail(mail);
            }
            userRepo.save(user);
        }else{
            response.setMessage("No changes to be made please proceed");
        }


        return response;
    }
    //Password reset
    public AuthResponse passwordReset(String email) {
        log.info("Reset password.");
  Optional<Users> user=userRepo.findByemail(email);
        String message="";
        AuthResponse responseModel=new AuthResponse();
  if (user.isPresent()){
      log.info("User with username: "+user.get().getUserName()+" found");
      Security updatedPassword=findPassword(user.get().getId().toString());
      updatedPassword.setStatus("RESET");
      updatedPassword.setEndDate(LocalDate.now());
      log.info("Generating and updating password ...");
      String password=randomString();
      log.info("actual password= {}",password);
      updatedPassword.setPassword(password);
      updateP(updatedPassword);
      log.info("password updated.");
      String variable[] = new String[]{
              user.get().getEmail(),user.get().getFirstName(),user.get().getUserName(),password
      };
      responseModel.setHttpStatus(HttpStatus.OK);
      responseModel.setHttpStatusCode(200);
      communication.resetPassword(variable);
      message= "Password have been reset! Please Check Your Email For New Login Credentials";

  }else {
      responseModel.setHttpStatusCode(404);
      responseModel.setHttpStatus(HttpStatus.NOT_FOUND);
       message="User with email: "+email+" does not exist";
       log.warn(message);

  }
  responseModel.setMessage(message);
return responseModel;
    }
    public AuthResponse findUser(String name){
        AuthResponse response=new AuthResponse();
        Optional<Users> user=userRepo.findByuserName(name);
         if (user.isPresent()){
             if (user.get().getActive()==Boolean.TRUE){
                 response.setHttpStatusCode(200);
                 response.setHttpStatus(HttpStatus.OK);
             }else {
                 response.setHttpStatusCode(403);
                 response.setReason("AccountLocked contact your administrator");
                 response.setHttpStatus(HttpStatus.OK);
             }
         }else {
             response.setReason("User "+name+" Not Found");
             response.setHttpStatusCode(404);
             response.setHttpStatus(HttpStatus.OK);
         }
        return  response;
    }
//authenticate
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException{

        Optional<Users> user=userRepo.findByuserName(name);
        login loggedUser = new login();

        if (user.isPresent()){
            log.info("Auth");
            Optional<Security> security = securityRepo.findByuserId(user.get().getId().toString());
            String roleId=user.get().getRoleId();
            Roles role =getRoleById(roleId);

            loggedUser.userName=user.get().getUserName();
            loggedUser.password=security.get().getPassword();

            UserDetails user1=new org.springframework.security.core.userdetails.User(loggedUser.userName, loggedUser.password,
                    this.addAuthorities(role));
            return user1;

        }else{

            log.warn("User: "+name+" Not found.");
            throw  new BadCredentialsException("user: "+name+" not found");
        }

    }

    public Collection<SimpleGrantedAuthority > addAuthorities(Roles roles){
        Collection<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
          roles.getPermissions().forEach(permission->{
              grantedAuthorities.add(new SimpleGrantedAuthority(permission.getValue()));
          });

        //grantedAuthorities.add(new SimpleGrantedAuthority("CUSTOMER"));

        return grantedAuthorities;
    }


    public AuthResponse changePassword(String userId, String newPassword) {
        AuthResponse responseModel=new AuthResponse();
        log.info("change password");
        Security security=findPassword(userId);
        security.setPassword(newPassword);
        responseModel=updateP(security);

        return responseModel;
    }

    //Roles and permissions
    public Roles createRole(Roles role){
        role.setCreatedAt(LocalDateTime.now());

            return roles.save(role);

    }

    public Roles updateRole(Roles role){
        return roles.save(role);
    }


    public List<Roles> getAllRoles(){ return roles.findAll(); }
    public Roles getRoleById(String id){
        return roles.findById(Long.parseLong(id)).get();
    }
    public rolePermissions createPermission(rolePermissions permission){
        return permissions.save(permission);

    }
    public List<rolePermissions> getAllPermissions(){
        return permissions.findAll();
    }

    public Optional<Roles> findRoleById(Long id){
        return roles.findById(id);
    }
    public Optional<rolePermissions> findPermissionById(Long id){
        return permissions.findById(id);
    }


}

