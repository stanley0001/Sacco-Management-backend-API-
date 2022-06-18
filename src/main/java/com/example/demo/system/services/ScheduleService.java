package com.example.demo.system.services;

import com.example.demo.loanManagement.services.LoanAccountService;
import com.example.demo.system.parsitence.models.Schedule.Schedule;
import com.example.demo.system.parsitence.repositories.ScheduleRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
@EnableScheduling
public class ScheduleService {
    private final ScheduleRepo scheduleRepo;
    public final LoanAccountService loanAccountService;

    public ScheduleService(ScheduleRepo scheduleRepo, LoanAccountService loanAccountService) {
        this.scheduleRepo = scheduleRepo;
        this.loanAccountService = loanAccountService;
    }
    //delete a schedule
    public void deleteSchedule(Long id){
        scheduleRepo.deleteById(id);
    }
    //update a schedule
    @Scheduled(fixedRate = 100L)
    public void schedule(){
        //check schedules
       // Optional<List<Schedule>> schedule=scheduleRepo.findByScheduleTimeLessThan(LocalDateTime.now());
        List<Schedule> schedule=scheduleRepo.findAll();
        for (Schedule schedule1:schedule
             ) {
            if (schedule1.getScheduleTime().isBefore(LocalDateTime.now())){
                //change status to default
                Boolean action=loanAccountService.defaultAccount(schedule1.getAccountNumber());
                log.info(action);
                if (action==Boolean.TRUE){
                    log.info("deleting the schedule");
                    deleteSchedule(schedule1.getId());
                }
            }
        }


        //log.info(time);


        //logic
        //schedule action
        }


}
