package com.ems.scheduler;

import static j2html.TagCreator.body;
import static j2html.TagCreator.each;
import static j2html.TagCreator.html;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

import java.util.List;

import org.ems.config.listener.AppContextAware;
import org.ems.dao.PollingDetailsDAO;
import org.joda.time.LocalDateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.ems.UI.dto.EmailDTO;
import com.ems.mailer.EmailUtil;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSUtility;

public class FailedDevicesJob extends AbstractJob {

	private static final Logger logger = LoggerFactory.getLogger(FailedDevicesJob.class);
	private PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);

	private EmailDTO emailDTO = null;

	public FailedDevicesJob() {
		// Do initialization here
	}

	@Override
	protected void preProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing required parameters - Failed devices...");
		this.emailDTO = ConfigHelper.getEmailDetails();
		this.emailDTO.setSubject("Failed device Details");
		logger.debug("Initializing required parameters completed - Failed devices...");
	}

	@Override
	protected void work(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing report preparation- Failed devices report...");

		this.emailDTO.setDate(
				EMSUtility.getFormattedTime(LocalDateTime.now().minusDays(1).toDate().getTime(), "dd-MMM-yyyy hh:mm"));

		List<String> devices = dao.loadFailedDevicesNames();

		String mailBody = html(
				body(table(tbody(tr(th("Device Names")), each(devices, device -> tr(td(device))))).attr("border", "2")))
						.render();

		// Send mail when there are some failed devices
		this.emailDTO.setSendMail(!devices.isEmpty());
		this.emailDTO.setBody(mailBody);

		logger.debug("Initializing report preparation completed- Failed devices report...");
	}

	@Override
	protected void postProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Trigger mail for Failed devices report..");
		// Set company name date and so on
		EmailUtil.setEmailDetails(this.emailDTO);

		if (this.emailDTO.isSendMail()) {
			boolean sent = EmailUtil.sendEmail(this.emailDTO);
		}

		logger.debug("mail triggered  for Failed devices report.. {}", this.emailDTO.isSendMail());
	}

	public static void main(String[] args) throws JobExecutionException {

		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(
				"D:/GitRepo/EMS_Repo/EMS/EMSService/src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml");
		AbstractJob job = new FailedDevicesJob();
		job.execute(null);

		context.close();
	}

}