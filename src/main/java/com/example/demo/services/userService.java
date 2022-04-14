package com.example.demo.services;

import com.example.demo.model.*;
import com.example.demo.persistence.repository.*;
import com.example.demo.persistence.entities.Users;
import lombok.extern.log4j.Log4j2;
import net.bytebuddy.utility.RandomString;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Log4j2
public class userService implements UserDetailsService {
  public final userRepo userRepo;
  public final securityRepo securityRepo;
  public final loginsRepo loginsRepo;
  public final rolesRepo roles;
  public final permissionsRepo permissions;
  public final CommunicationService communication;

  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    public userService(com.example.demo.persistence.repository.userRepo userRepo, com.example.demo.persistence.repository.securityRepo securityRepo, com.example.demo.persistence.repository.loginsRepo loginsRepo, rolesRepo roles, permissionsRepo permissions, CommunicationService communication) {
        this.userRepo = userRepo;
        this.securityRepo = securityRepo;
        this.loginsRepo = loginsRepo;
        this.roles = roles;
        this.permissions = permissions;
        this.communication = communication;
    }
      public String randomString(){
          RandomString generatedString = new RandomString();
          log.info("Generating a random string.");
          return  generatedString.nextString();
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
         communication.getData(variable);
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
    public ResponseModel updateP(Security security) {
        ResponseModel responseModel=new ResponseModel();
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
    public ResponseModel changeUserStatus(Long id, Boolean status){
        ResponseModel response=new ResponseModel();
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
    public ResponseModel passwordReset(String email) {
        log.info("Reset password.");
  Optional<Users> user=userRepo.findByemail(email);
        String message="";
        ResponseModel responseModel=new ResponseModel();
  if (user.isPresent()){
      log.info("User with username: "+user.get().getUserName()+" found");
      Security updatedPassword=findPassword(user.get().getId().toString());
      updatedPassword.setStatus("RESET");
      updatedPassword.setEndDate(LocalDate.now());
      log.info("Generating and updating password ...");
      String password=randomString();
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
    public ResponseModel findUser(String name){
        ResponseModel response=new ResponseModel();
        Optional<Users> user=userRepo.findByuserName(name);
         if (user.isPresent()){
             if (user.get().getActive()==Boolean.TRUE){
                 response.setHttpStatusCode(200);
                 response.setHttpStatus(HttpStatus.OK);
             }else {
                 response.setHttpStatusCode(403);
                 response.setReason("AccountLocked contact your administrator");
                 response.setHttpStatus(HttpStatus.FORBIDDEN);
             }
         }else {
             response.setReason("User "+name+" Not Found");
             response.setHttpStatusCode(404);
             response.setHttpStatus(HttpStatus.NOT_FOUND);
         }
        return  response;
    }
//authenticate
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException{
        log.info("Route:.../login..");
       Optional<Users> user=userRepo.findByuserName(name);
        login loggedUser = new login();
        Set < GrantedAuthority > grantedAuthorities = new HashSet < > ();
        if (user.isPresent()){
            log.info("Trying to authenticate User: "+user.get().getUserName()+"...");
            Optional<Security> security = securityRepo.findByuserId(user.get().getId().toString());

            String roleId=user.get().getRoleId();
            Optional<Roles> role =getRoleById(roleId);
            grantedAuthorities.add(new SimpleGrantedAuthority(role.get().getRoleName()));
            loggedUser.userName=user.get().getUserName();
            loggedUser.password=security.get().getPassword();
            log.info("please wait....");
            Boolean accountActive=user.get().getActive();

            UserDetails user1=new org.springframework.security.core.userdetails.User(loggedUser.userName, loggedUser.password,
                    grantedAuthorities);

            return user1;

        }else{

            log.warn("User: "+name+" Not found.");
            throw  new BadCredentialsException("user: "+name+" not found");
        }

    }
    public ResponseModel changePassword(String userId, String newPassword) {
        ResponseModel responseModel=new ResponseModel();
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
    public Optional<Roles> getRoleById(String id){
        return roles.findById(Long.parseLong(id));
    }
    public rolePermissions createPermission(rolePermissions permission){
        return permissions.save(permission);
    }
    public rolePermissions updatePermission(rolePermissions permission){
        return permissions.save(permission);
    }
    public Optional<Roles> findRoleById(Long id){
        return roles.findById(id);
    }
    public Optional<rolePermissions> findPermissionById(Long id){
        return permissions.findById(id);
    }



}

