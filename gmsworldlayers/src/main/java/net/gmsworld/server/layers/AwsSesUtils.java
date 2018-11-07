package net.gmsworld.server.layers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;

public class AwsSesUtils {
	
	private static final Logger logger = Logger.getLogger(AwsSesUtils.class.getName());
	
	private static final Regions DEFAULT_REGION =  Regions.US_EAST_1;
	
	private static Map<String, AmazonSimpleEmailService> sesClients = new HashMap<String, AmazonSimpleEmailService>();
	
	private static AmazonSimpleEmailService getSesClient(Regions region) {
		 if (!sesClients.containsKey(region.getName())) {
				BasicAWSCredentials awsCredentials = new BasicAWSCredentials(Commons.getProperty(Property.AWS_ACCESS_KEY), Commons.getProperty(Property.AWS_ACCESS_SECRET)); 
				
				AmazonSimpleEmailService sesClient =  AmazonSimpleEmailServiceClientBuilder.standard().
						withRegion(region). 
						withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
				
				sesClients.put(region.getName(), sesClient);
				return sesClient;
		 } else {
			 return sesClients.get(region.getName());
		 }
	}
	
	
	public static boolean sendEmail(String from, final String fromNick, String to, final String toNick, String cc, final String ccNick, final String message, final String contentType, final String title) {
		if (StringUtils.isNotEmpty(from) && StringUtils.isNotEmpty(to) && StringUtils.isNotEmpty(title) && message != null) {

			if (StringUtils.isNotEmpty(toNick)) {
				 to = "\"" + toNick + "\" <" + to + ">";
			}
			
			if (StringUtils.isNotEmpty(fromNick)) {
				 from = "\"" + fromNick + "\" <" + from + ">";
			}
			
			Destination dest = new Destination().withToAddresses(to);
			
			if (StringUtils.isNotEmpty(cc)) {
				if (StringUtils.isNotEmpty(ccNick)) {
					 cc = "\"" + ccNick + "\" <" + cc + ">";
				}
				dest.withCcAddresses(cc);
			}
		
			Body body = null;
			
			if (StringUtils.contains(contentType, "html")) {
				body = new Body().withHtml(new Content().withData(message));
			} else {
				body = new Body().withText(new Content().withData(message));
			}
			
			boolean status = false;
			try {
				SendEmailResult result = getSesClient(DEFAULT_REGION ).sendEmail(new SendEmailRequest().
					withDestination(dest).
					withMessage(new Message().withBody(body).withSubject(new Content().withData(title))).
					withSource(from));
				logger.log(Level.INFO, "Message sent with id: " + result.getMessageId());
				status = true;
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				status = false;
			}
			
			return status;
		} else {
			logger.log(Level.SEVERE, "Missing required parameter!");
			return false;
		}
	}
}
