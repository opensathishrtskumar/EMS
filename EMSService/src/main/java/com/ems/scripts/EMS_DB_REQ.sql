DB Env setup : 
	1. Create Scheme		
		1 CREATE DATABASE `setup`;
		2 CREATE DATABASE `polling`;
		  CREATE DATABASE `monthly`;
		  CREATE DATABASE `archive`;
				
		3 CREATE TABLE `setup`.`devicedetails`( `deviceuniqueid` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, 
					`unitid` VARCHAR(5) NOT NULL, `devicealiasname` VARCHAR(50) NOT NULL, 
					`baudrate` INT UNSIGNED NOT NULL, 
					`wordlength` SMALLINT UNSIGNED NOT NULL, 
					`stopbit` SMALLINT UNSIGNED NOT NULL, 
					`parity` VARCHAR(10) NOT NULL, 
					`memorymapping` VARCHAR(1000) NOT NULL, 
					`status` BOOL NOT NULL, 
					`registermapping` VARCHAR(5) DEFAULT 'MSRF' NOT NULL,
					`port` VARCHAR(5) DEFAULT 'COM3' NOT NULL,
					`method` VARCHAR(5) DEFAULT '3' NOT NULL,
					`createdtime` BIGINT UNSIGNED NOT NULL, 
					`modifiedtime` BIGINT UNSIGNED NOT NULL, PRIMARY KEY (`deviceuniqueid`) );
					
		
		4 CREATE TABLE `polling`.`pollingdetails`( `deviceuniqueid` BIGINT UNSIGNED NOT NULL, 
					`polledon` BIGINT UNSIGNED NOT NULL, 
					`unitresponse` VARCHAR(2000), KEY (`deviceuniqueid`)); 
		
			ALTER TABLE `polling`.`pollingdetails` ADD  KEY `polledon` (`polledon`);
			
			-- Archive data
		 4.1 CREATE TABLE `archive`.`pollingdetails`(  
			  `deviceuniqueid` BIGINT UNSIGNED NOT NULL,
			  `polledon` BIGINT UNSIGNED NOT NULL,
			  `unitresponse` VARCHAR(2000) NOT NULL
			) ENGINE=MYISAM;

			--Month old data
		 4.2 CREATE TABLE `monthly`.`pollingdetails`(  
			  `deviceuniqueid` BIGINT UNSIGNED,
			  `polledon` BIGINT UNSIGNED,
			  `unitresponse` VARCHAR(2000),
			  INDEX (`deviceuniqueid`),
			  INDEX (`polledon`)
			) ENGINE=MYISAM;
	
			
		5 CREATE TABLE `polling`.`recentpoll`(  
  					`deviceuniqueid` BIGINT UNSIGNED NOT NULL,
  					`polledon` BIGINT UNSIGNED NOT NULL,
  					`unitresponse` TEXT,
  					`status` BOOL NOT NULL
				);
			
		6 CREATE TABLE `setup`.`settings`(  
			  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
			  `skey` VARCHAR(100) NOT NULL,
			  `svalue` VARCHAR(1000) NOT NULL,
			  PRIMARY KEY (`id`)
			) ENGINE=MYISAM;
			
			INSERT INTO setup.settings(skey,svalue) VALUES('dashboardrefreshfrequency','15000');
			INSERT INTO setup.settings(skey,svalue) VALUES('bccEmail','ems.ses03@gmail.com');
			INSERT INTO setup.settings(skey,svalue) VALUES('ccEmail','ems.ses03@gmail.com');
			INSERT INTO setup.settings(skey,svalue) VALUES('fromEmail','ems.ses03@gmail.com');
			INSERT INTO setup.settings(skey,svalue) VALUES('mailPassword','kavi071215');
			INSERT INTO setup.settings(skey,svalue) VALUES('port','25');
			INSERT INTO setup.settings(skey,svalue) VALUES('smtpHost','smtp.gmail.com');
			INSERT INTO setup.settings(skey,svalue) VALUES('toEmail','sathishrtskumar@gmail.com');
			INSERT INTO setup.settings(skey,svalue) VALUES('companyName','ISUZU');
			INSERT INTO setup.settings(skey,svalue) VALUES('reportPath','C:\\Users\\Gokul.m\\Reports');
			INSERT INTO setup.settings(skey,svalue) VALUES('backupPath','C:\\Users\\Gokul.m\\Backup');
			
			
			--For final report settings begins
			INSERT INTO setup.settings(skey,svalue) VALUES('main_incomer','36');--deviceuniqueid
			INSERT INTO setup.settings(skey,svalue) VALUES('dg_incomer','49');--deviceuniqueid
			INSERT INTO setup.settings(skey,svalue) VALUES('body_shop1','35');--deviceuniqueid
			INSERT INTO setup.settings(skey,svalue) VALUES('body_shop2','54');--deviceuniqueid
			
			INSERT INTO setup.settings(skey,svalue) VALUES('paint_shop1','33');--deviceuniqueid
			INSERT INTO setup.settings(skey,svalue) VALUES('paint_shop2','25');--deviceuniqueid
			
			INSERT INTO setup.settings(skey,svalue) VALUES('ga_shop1','37');--deviceuniqueid
			INSERT INTO setup.settings(skey,svalue) VALUES('ga_shop2','55');--deviceuniqueid
			
			INSERT INTO setup.settings(skey,svalue) VALUES('utility1','39');--deviceuniqueid
			INSERT INTO setup.settings(skey,svalue) VALUES('utility2','20');--deviceuniqueid
			
			INSERT INTO setup.settings(skey,svalue) VALUES('office1','38');--deviceuniqueid
			INSERT INTO setup.settings(skey,svalue) VALUES('office2','48');--deviceuniqueid
			
			INSERT INTO setup.settings(skey,svalue) VALUES('press_shop1','40');--deviceuniqueid
			--For final report settings ends
			
