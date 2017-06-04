package net.gmsworld.server.layers;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;

public class AwsSesUtils {
	
	public static void sendEmail(final String email, final String message, final String title) {
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(Commons.getProperty(Property.AWS_ACCESS_KEY), Commons.getProperty(Property.AWS_ACCESS_SECRET)); 
		
		AmazonSimpleEmailService sesClient =  AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();

        sesClient.sendEmail(new SendEmailRequest().
                withDestination(new Destination().withToAddresses(email)).
                withMessage(new Message().withBody(new Body().withText(new Content().withData(message))).withSubject(new Content().withData(title))).
                withSource(ConfigurationManager.SUPPORT_MAIL));
	}
}
