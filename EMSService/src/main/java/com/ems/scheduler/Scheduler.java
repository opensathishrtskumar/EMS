package com.ems.scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.quartz.SchedulerException;

import com.ems.UI.dto.AttachmentDTO;
import com.ems.UI.dto.EmailDTO;
import com.ems.mailer.EmailUtil;
import com.ems.util.EMSUtility;

public class Scheduler {

	public static void main(String[] args) throws SchedulerException, InterruptedException {
		
		/*JobDetail job = SchedulerUtil.createJob("HelloJob", "EMS", DailyReportJob.class);
		Trigger trigger = SchedulerUtil.createTrigger("DailyReport", "EMS", "0/5 * * * * ?");
		SchedulerConfigurer.scheduleJob(trigger, job);*/
		
		EmailDTO dto = new EmailDTO();
		
		dto.setBody("<html><head><title>Saratha Engineering</title></head><body><h1>Isuzu Report</h1></body></html>");
		//dto.setCcEmail("gokul@sarathaeng.com");
		dto.setFromEmail("ems.ses03@gmail.com");
		dto.setMailPassword("kavi071215");
		dto.setSubject("EMS Report Test");
		dto.setToEmail("sarathaengg@gmail.com,sathishrtskumar@gmail.com");
		
		List<AttachmentDTO> list = new ArrayList<>();
		list.add(new AttachmentDTO(new File("C:\\Users\\Gokul.m\\Desktop\\Isuzu.xlsx"),"Attach1.xlsx"));
		list.add(new AttachmentDTO(new File("C:\\Users\\Gokul.m\\Desktop\\Isuzu.xlsx"),"Attach2.xlsx"));
		
		dto.setAttachments(list);
		
		EmailUtil.sendEmail(dto);
		
		/*String s = EMSUtility.convertObjectToJSONString(new EmailDTO());
		System.out.println(s);*/
	}
}


